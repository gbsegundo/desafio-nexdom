# Nexdom Estoque

Aplicação de controle de estoque com API REST em Spring Boot e interface em Vue.js.

## O que a aplicação faz

- CRUD de produtos (código, descrição, tipo, valor no fornecedor e quantidade em estoque).
- Entrada e saída de estoque. A saída só é gravada se houver saldo. Caso contrário a API responde `422` com a mensagem `Saldo insuficiente para o produto {código}. Quantidade disponível: {n}. Quantidade solicitada: {m}.` e o saldo permanece igual.
- Consulta de produtos por tipo, com a quantidade total de saída e a quantidade disponível.
- Consulta de lucro por produto: quantidade total de saída e lucro `(valor de venda − valor do fornecedor) × quantidade`, somado em todas as saídas. Entrada não entra no lucro.

A quantidade informada no cadastro é o saldo de abertura. Ela não gera movimentação. A partir daí, só entrada e saída alteram o estoque. Atualizar o produto não mexe no saldo. Excluir o produto também remove o histórico de movimentações dele.

O banco é H2 em memória. Os dados existem enquanto a API está no ar. Na primeira subida, fora do perfil de teste, a aplicação carrega alguns produtos de exemplo.

## Tecnologias

- Java 25
- Spring Boot 4.2 (Web MVC, Spring Data JPA, Validation, H2)
- Maven 3.9, via wrapper (`mvnw`)
- Vue 3.5 e Vue Router 5, com Vite 8

## Como executar

É preciso o JDK 25. Nesta máquina ele foi instalado em:

`C:\Program Files\Eclipse Adoptium\jdk-25.0.5.10002-hotspot`

No PowerShell, suba a API:

```powershell
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-25.0.5.10002-hotspot"
$env:Path = "$env:JAVA_HOME\bin;" + $env:Path
cd backend
.\mvnw.cmd spring-boot:run
```

Em outro terminal, suba a interface:

```powershell
cd frontend
npm install
npm run dev
```

Abra `http://localhost:5173`. A interface encaminha `/api` para `http://localhost:8080`.

O console do H2 fica em `http://localhost:8080/h2-console`. JDBC URL: `jdbc:h2:mem:estoque`, usuário `sa`, senha em branco.

## Testes

```powershell
cd backend
.\mvnw.cmd test
```

Os testes de domínio e de serviço não sobem o Spring. `EstoqueApiTest` sobe a API com H2 e cobre cadastro, saída com saldo, saída recusada, consulta por tipo, lucro, atualização e exclusão.

No frontend, o Vitest roda em jsdom. A cobertura usa o provider v8 e grava o HTML em `frontend/coverage`.

```powershell
cd frontend
npm test
npm run test:coverage
```

`npm run test:watch` deixa a suíte observando os arquivos. O relatório cobre autenticação, formatação de moeda, cliente HTTP, rótulos, o campo de moeda e a guarda das rotas.

O projeto traz `backend/.mvn/settings.xml` apontando para o Maven Central. O wrapper usa esse arquivo, então o build não depende de um espelho Maven configurado na máquina.

## API

| Método | Caminho | Uso |
| --- | --- | --- |
| `POST` | `/api/produtos` | Cria produto |
| `GET` | `/api/produtos` | Lista produtos |
| `GET` | `/api/produtos/{id}` | Busca produto |
| `PUT` | `/api/produtos/{id}` | Atualiza dados cadastrais, sem alterar o saldo |
| `DELETE` | `/api/produtos/{id}` | Exclui produto e movimentações |
| `POST` | `/api/movimentos` | Registra entrada ou saída |
| `GET` | `/api/movimentos?produtoId=` | Lista movimentações |
| `GET` | `/api/consultas/produtos?tipo=ELETRONICO` | Saída e saldo por tipo |
| `GET` | `/api/consultas/lucro` | Lucro de todos os produtos |
| `GET` | `/api/consultas/lucro/{produtoId}` | Lucro de um produto |

Tipos de produto: `ELETRONICO`, `ELETRODOMESTICO`, `MOVEL`.

Tipos de movimentação: `ENTRADA`, `SAIDA`. Em saída, `valorVenda` é obrigatório. `dataVenda` é opcional e, se omitida, recebe a data e hora atuais.

Erros de negócio voltam neste formato:

```json
{
  "mensagem": "Saldo insuficiente para o produto NB-001. Quantidade disponível: 6. Quantidade solicitada: 10.",
  "codigo": "SALDO_INSUFICIENTE",
  "campos": {}
}
```

## Organização

O descritivo da arquitetura está em [backend/ARQUITETURA.md](backend/ARQUITETURA.md) e [frontend/ARQUITETURA.md](frontend/ARQUITETURA.md).

O backend separa o que muda por motivos diferentes:

- `domain` guarda produto, movimentação, saldo e o cálculo de lucro.
- `application` orquestra os casos de uso. Entrada e saída são estratégias (`ProcessadorMovimentacao`): um tipo novo de movimentação entra como outra implementação, sem alterar o serviço.
- `infrastructure` são os repositórios Spring Data.
- `api` são os controllers e o tratamento de erro HTTP.

A saída bloqueia a linha do produto durante a transação, para duas saídas simultâneas não consumirem o mesmo saldo.
