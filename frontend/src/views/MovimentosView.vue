<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import Alert from '../components/Alert.vue'
import CampoMoeda from '../components/CampoMoeda.vue'
import { api } from '../api/client'
import { labelMovimento } from '../api/labels'
import { dataHora, moeda, numeroDeMoeda } from '../utils/format'

const produtos = ref([])
const movimentos = ref([])
const erro = ref('')
const campos = ref({})
const sucesso = ref('')
const salvando = ref(false)
const carregando = ref(true)

const form = reactive({
  produtoId: '',
  tipoMovimentacao: 'ENTRADA',
  valorVenda: '',
  dataVenda: '',
  quantidade: 1,
})

const ehSaida = computed(() => form.tipoMovimentacao === 'SAIDA')
const produtoSelecionado = computed(() => produtos.value.find((item) => item.id === Number(form.produtoId)))
const saldoEstoura = computed(() =>
  ehSaida.value
  && produtoSelecionado.value
  && Number(form.quantidade) > Number(produtoSelecionado.value.quantidadeEstoque),
)

async function carregar() {
  const [listaProdutos, listaMovimentos] = await Promise.all([
    api('/produtos'),
    api('/movimentos'),
  ])
  produtos.value = listaProdutos
  movimentos.value = listaMovimentos
  if (!form.produtoId && listaProdutos.length) {
    form.produtoId = listaProdutos[0].id
  }
}

async function registrar() {
  erro.value = ''
  sucesso.value = ''
  campos.value = {}
  if (!form.produtoId) {
    erro.value = 'Selecione um produto.'
    return
  }
  const valorVenda = numeroDeMoeda(form.valorVenda)
  if (ehSaida.value && valorVenda === null) {
    erro.value = 'Informe o valor de venda para a saída.'
    return
  }

  salvando.value = true
  try {
    await api('/movimentos', {
      method: 'POST',
      body: JSON.stringify({
        produtoId: Number(form.produtoId),
        tipoMovimentacao: form.tipoMovimentacao,
        quantidade: Number(form.quantidade),
        valorVenda: ehSaida.value ? valorVenda.toFixed(2) : null,
        dataVenda: form.dataVenda
          ? (form.dataVenda.length === 16 ? `${form.dataVenda}:00` : form.dataVenda)
          : null,
      }),
    })
    sucesso.value = ehSaida.value ? 'Saída registrada e saldo atualizado.' : 'Entrada registrada e saldo atualizado.'
    form.quantidade = 1
    form.valorVenda = ''
    form.dataVenda = ''
    await carregar()
  } catch (error) {
    erro.value = error.message
    campos.value = error.campos || {}
  } finally {
    salvando.value = false
  }
}

onMounted(async () => {
  try {
    await carregar()
  } catch (error) {
    erro.value = error.message
  } finally {
    carregando.value = false
  }
})
</script>

<template>
  <div class="stack">
    <Alert :mensagem="erro" :campos="campos" />
    <Alert tipo="ok" :mensagem="sucesso" />

    <form class="panel" @submit.prevent="registrar">
      <div class="panel-head">
        <div>
          <h2>Nova movimentação</h2>
          <p>Escolha o produto, o tipo e a quantidade. Na saída, informe também o valor de venda.</p>
        </div>
      </div>

      <div class="segmented" role="radiogroup" aria-label="Tipo de movimentação">
        <button type="button" :class="{ on: !ehSaida }" @click="form.tipoMovimentacao = 'ENTRADA'">Entrada</button>
        <button type="button" :class="{ on: ehSaida }" @click="form.tipoMovimentacao = 'SAIDA'">Saída</button>
      </div>

      <div class="grid" style="margin-top: 16px">
        <label class="field wide">
          <span class="field-label">Produto</span>
          <select v-model="form.produtoId" required>
            <option v-for="produto in produtos" :key="produto.id" :value="produto.id">
              {{ produto.codigo }} — {{ produto.descricao }}
            </option>
          </select>
        </label>
        <label class="field">
          <span class="field-label">Quantidade</span>
          <input v-model.number="form.quantidade" type="number" min="1" step="1" required />
        </label>
        <label v-if="ehSaida" class="field">
          <span class="field-label">Valor de venda</span>
          <CampoMoeda v-model="form.valorVenda" obrigatorio :invalido="Boolean(campos.valorVenda)" />
        </label>
        <label class="field" :class="{ wide: !ehSaida }">
          <span class="field-label">Data da venda</span>
          <input v-model="form.dataVenda" type="datetime-local" />
          <span class="hint">Se ficar em branco, usamos a data e a hora de agora.</span>
        </label>
      </div>

      <div v-if="produtoSelecionado" class="balance" :class="{ warn: saldoEstoura }" style="margin-top: 16px">
        <div>
          <span class="hint">Disponível em {{ produtoSelecionado.codigo }}</span>
          <strong>{{ produtoSelecionado.quantidadeEstoque }}</strong>
        </div>
        <p v-if="saldoEstoura" class="hint">
          A quantidade pedida passa do saldo. A saída será recusada e o estoque não muda.
        </p>
      </div>

      <div class="actions">
        <button class="primary" :class="{ saida: ehSaida }" type="submit" :disabled="salvando || carregando">
          {{ salvando ? 'Registrando...' : ehSaida ? 'Registrar saída' : 'Registrar entrada' }}
        </button>
      </div>
    </form>

    <section class="panel">
      <div class="panel-head">
        <div>
          <h2>Histórico</h2>
          <p>{{ movimentos.length }} movimentação(ões)</p>
        </div>
      </div>
      <div class="table-wrap">
        <table>
          <thead>
            <tr>
              <th>Data</th>
              <th>Produto</th>
              <th>Tipo</th>
              <th class="num">Quantidade</th>
              <th class="num">Valor de venda</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="carregando">
              <td colspan="5" class="empty">Carregando movimentações...</td>
            </tr>
            <tr v-else-if="!movimentos.length">
              <td colspan="5" class="empty">Nenhuma movimentação registrada.</td>
            </tr>
            <tr v-for="movimento in movimentos" :key="movimento.id">
              <td>{{ dataHora(movimento.dataVenda) }}</td>
              <td><span class="code">{{ movimento.produtoCodigo }}</span> {{ movimento.produtoDescricao }}</td>
              <td>
                <span class="pill" :class="movimento.tipoMovimentacao === 'SAIDA' ? 'out' : 'in'">
                  {{ labelMovimento(movimento.tipoMovimentacao) }}
                </span>
              </td>
              <td class="num">{{ movimento.quantidade }}</td>
              <td class="num">{{ moeda(movimento.valorVenda) }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </section>
  </div>
</template>
