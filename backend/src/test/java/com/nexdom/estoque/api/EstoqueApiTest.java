package com.nexdom.estoque.api;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class EstoqueApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void controlaSaldoConsultaPorTipoECalculaLucro() throws Exception {
        String codigo = codigoUnico();
        long id = criarProduto(codigo, 10);

        mockMvc.perform(post("/api/movimentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "produtoId": %d,
                                  "tipoMovimentacao": "SAIDA",
                                  "valorVenda": 3200.00,
                                  "dataVenda": "2026-11-20T10:00:00",
                                  "quantidade": 4
                                }
                                """.formatted(id)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.produtoCodigo").value(codigo))
                .andExpect(jsonPath("$.quantidade").value(4));

        mockMvc.perform(get("/api/produtos/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantidadeEstoque").value(6));

        mockMvc.perform(post("/api/movimentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "produtoId": %d,
                                  "tipoMovimentacao": "SAIDA",
                                  "valorVenda": 3300.00,
                                  "quantidade": 10
                                }
                                """.formatted(id)))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.codigo").value("SALDO_INSUFICIENTE"))
                .andExpect(jsonPath("$.mensagem").value(
                        "Saldo insuficiente para o produto %s. Quantidade disponível: 6. Quantidade solicitada: 10."
                                .formatted(codigo)
                ));

        mockMvc.perform(get("/api/produtos/{id}", id))
                .andExpect(jsonPath("$.quantidadeEstoque").value(6));

        mockMvc.perform(get("/api/consultas/produtos").param("tipo", "ELETRONICO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.codigo == '%s')].quantidadeSaida".formatted(codigo)).value(4))
                .andExpect(jsonPath("$[?(@.codigo == '%s')].quantidadeDisponivel".formatted(codigo)).value(6));

        mockMvc.perform(get("/api/consultas/lucro/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantidadeTotalSaida").value(4))
                .andExpect(jsonPath("$.totalLucro").value(2800.00));

        mockMvc.perform(put("/api/produtos/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "codigo": "%s",
                                  "descricao": "Notebook atualizado",
                                  "tipoProduto": "ELETRONICO",
                                  "valorFornecedor": 2500.00
                                }
                                """.formatted(codigo)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.descricao").value("Notebook atualizado"))
                .andExpect(jsonPath("$.quantidadeEstoque").value(6));

        mockMvc.perform(delete("/api/produtos/{id}", id))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/produtos/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensagem").value("Produto não encontrado."));
    }

    @Test
    void recusaSaidaSemValorDeVendaECodigoDuplicado() throws Exception {
        String codigo = codigoUnico();
        long id = criarProduto(codigo, 2);

        mockMvc.perform(post("/api/movimentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "produtoId": %d,
                                  "tipoMovimentacao": "SAIDA",
                                  "quantidade": 1
                                }
                                """.formatted(id)))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.mensagem").value("Valor de venda é obrigatório para saída de estoque."));

        mockMvc.perform(post("/api/produtos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(produtoJson(codigo, 0)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensagem").value("Já existe um produto com o código " + codigo + "."));
    }

    @Test
    void recusaProdutoSemCodigo() throws Exception {
        mockMvc.perform(post("/api/produtos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "descricao": "Sem código",
                                  "tipoProduto": "MOVEL",
                                  "valorFornecedor": 10.00,
                                  "quantidadeEstoque": 1
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("DADOS_INVALIDOS"))
                .andExpect(jsonPath("$.campos.codigo").value("Código é obrigatório."));
    }

    private long criarProduto(String codigo, int quantidade) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/produtos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(produtoJson(codigo, quantidade)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.codigo").value(codigo))
                .andReturn();
        Number id = JsonPath.read(result.getResponse().getContentAsString(), "$.id");
        return id.longValue();
    }

    private String produtoJson(String codigo, int quantidade) {
        return """
                {
                  "codigo": "%s",
                  "descricao": "Notebook de teste",
                  "tipoProduto": "ELETRONICO",
                  "valorFornecedor": 2500.00,
                  "quantidadeEstoque": %d
                }
                """.formatted(codigo, quantidade);
    }

    private String codigoUnico() {
        return "T-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
