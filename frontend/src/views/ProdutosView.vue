<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import Alert from '../components/Alert.vue'
import CampoMoeda from '../components/CampoMoeda.vue'
import ConfirmDialog from '../components/ConfirmDialog.vue'
import { api } from '../api/client'
import { TIPOS_PRODUTO, labelTipo } from '../api/labels'
import { moeda, moedaCampo, numeroDeMoeda } from '../utils/format'

const produtos = ref([])
const erro = ref('')
const campos = ref({})
const sucesso = ref('')
const salvando = ref(false)
const carregando = ref(true)
const editandoId = ref(null)
const busca = ref('')
const filtroTipo = ref('TODOS')
const exclusao = ref(null)

const form = reactive({
  codigo: '',
  descricao: '',
  tipoProduto: 'ELETRONICO',
  valorFornecedor: '',
  quantidadeEstoque: 0,
})

const visiveis = computed(() => {
  const termo = busca.value.trim().toLowerCase()
  return produtos.value.filter((produto) => {
    const texto = `${produto.codigo} ${produto.descricao}`.toLowerCase()
    const combinaBusca = texto.includes(termo)
    const combinaTipo = filtroTipo.value === 'TODOS' || produto.tipoProduto === filtroTipo.value
    return combinaBusca && combinaTipo
  })
})

function limparFormulario() {
  editandoId.value = null
  form.codigo = ''
  form.descricao = ''
  form.tipoProduto = 'ELETRONICO'
  form.valorFornecedor = ''
  form.quantidadeEstoque = 0
}

async function carregar() {
  produtos.value = await api('/produtos')
}

function editar(produto) {
  erro.value = ''
  sucesso.value = ''
  campos.value = {}
  editandoId.value = produto.id
  form.codigo = produto.codigo
  form.descricao = produto.descricao
  form.tipoProduto = produto.tipoProduto
  form.valorFornecedor = moedaCampo(produto.valorFornecedor)
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

async function salvar() {
  erro.value = ''
  sucesso.value = ''
  campos.value = {}
  const valorFornecedor = numeroDeMoeda(form.valorFornecedor)
  if (valorFornecedor === null) {
    erro.value = 'Informe o valor no fornecedor.'
    return
  }

  salvando.value = true
  try {
    const payload = {
      codigo: form.codigo,
      descricao: form.descricao,
      tipoProduto: form.tipoProduto,
      valorFornecedor: valorFornecedor.toFixed(2),
    }
    if (editandoId.value) {
      await api(`/produtos/${editandoId.value}`, { method: 'PUT', body: JSON.stringify(payload) })
      sucesso.value = 'Produto atualizado. A quantidade em estoque permanece a mesma.'
    } else {
      payload.quantidadeEstoque = Number(form.quantidadeEstoque)
      await api('/produtos', { method: 'POST', body: JSON.stringify(payload) })
      sucesso.value = 'Produto cadastrado.'
    }
    limparFormulario()
    await carregar()
  } catch (error) {
    erro.value = error.message
    campos.value = error.campos || {}
  } finally {
    salvando.value = false
  }
}

async function confirmarExclusao() {
  const produto = exclusao.value
  exclusao.value = null
  erro.value = ''
  sucesso.value = ''
  try {
    await api(`/produtos/${produto.id}`, { method: 'DELETE' })
    if (editandoId.value === produto.id) {
      limparFormulario()
    }
    sucesso.value = `${produto.codigo} foi excluído, junto com o histórico de movimentações.`
    await carregar()
  } catch (error) {
    erro.value = error.message
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

    <form class="panel" @submit.prevent="salvar">
      <div class="panel-head">
        <div>
          <h2>{{ editandoId ? 'Editar produto' : 'Novo produto' }}</h2>
          <p v-if="editandoId">Você está editando {{ form.codigo }}. O saldo só muda por entrada ou saída.</p>
          <p v-else>A quantidade inicial entra como saldo de abertura.</p>
        </div>
      </div>
      <div class="grid">
        <label class="field">
          <span class="field-label">Código</span>
          <input v-model="form.codigo" maxlength="30" placeholder="Ex.: NB-001" required :aria-invalid="Boolean(campos.codigo)" />
        </label>
        <label class="field">
          <span class="field-label">Tipo</span>
          <select v-model="form.tipoProduto" :aria-invalid="Boolean(campos.tipoProduto)">
            <option v-for="tipo in TIPOS_PRODUTO" :key="tipo.value" :value="tipo.value">{{ tipo.label }}</option>
          </select>
        </label>
        <label class="field wide">
          <span class="field-label">Descrição</span>
          <input v-model="form.descricao" maxlength="200" placeholder="Nome claro do produto" required :aria-invalid="Boolean(campos.descricao)" />
        </label>
        <label class="field">
          <span class="field-label">Valor no fornecedor</span>
          <CampoMoeda v-model="form.valorFornecedor" obrigatorio :invalido="Boolean(campos.valorFornecedor)" />
        </label>
        <label v-if="!editandoId" class="field">
          <span class="field-label">Quantidade em estoque</span>
          <input v-model.number="form.quantidadeEstoque" type="number" min="0" step="1" required />
        </label>
      </div>
      <div class="actions">
        <button class="primary" type="submit" :disabled="salvando">
          {{ salvando ? 'Salvando...' : editandoId ? 'Salvar alterações' : 'Cadastrar produto' }}
        </button>
        <button v-if="editandoId" class="ghost" type="button" @click="limparFormulario">Cancelar edição</button>
      </div>
    </form>

    <section class="panel">
      <div class="panel-head">
        <div>
          <h2>Catálogo</h2>
          <p>{{ visiveis.length }} de {{ produtos.length }} produto(s)</p>
        </div>
      </div>
      <div class="toolbar">
        <label class="field">
          <span class="field-label">Buscar</span>
          <input v-model="busca" type="search" placeholder="Código ou descrição" />
        </label>
      </div>
      <div class="chips" role="group" aria-label="Filtrar por tipo">
        <button type="button" class="chip" :class="{ on: filtroTipo === 'TODOS' }" @click="filtroTipo = 'TODOS'">Todos</button>
        <button
          v-for="tipo in TIPOS_PRODUTO"
          :key="tipo.value"
          type="button"
          class="chip"
          :class="{ on: filtroTipo === tipo.value }"
          @click="filtroTipo = tipo.value"
        >
          {{ tipo.label }}
        </button>
      </div>
      <div class="table-wrap" style="margin-top: 16px">
        <table>
          <thead>
            <tr>
              <th>Código</th>
              <th>Descrição</th>
              <th>Tipo</th>
              <th class="num">Valor fornecedor</th>
              <th class="num">Estoque</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="carregando">
              <td colspan="6" class="empty">Carregando produtos...</td>
            </tr>
            <tr v-else-if="!visiveis.length">
              <td colspan="6" class="empty">Nenhum produto encontrado com esse filtro.</td>
            </tr>
            <tr v-for="produto in visiveis" :key="produto.id">
              <td class="code">{{ produto.codigo }}</td>
              <td>{{ produto.descricao }}</td>
              <td>{{ labelTipo(produto.tipoProduto) }}</td>
              <td class="num">{{ moeda(produto.valorFornecedor) }}</td>
              <td class="num"><span class="badge" :class="{ zero: produto.quantidadeEstoque === 0 }">{{ produto.quantidadeEstoque }}</span></td>
              <td class="row-actions">
                <button type="button" class="ghost" @click="editar(produto)">Editar</button>
                <button type="button" class="text-danger" @click="exclusao = produto">Excluir</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </section>

    <ConfirmDialog
      :aberto="Boolean(exclusao)"
      titulo="Excluir produto"
      :texto="exclusao ? `Excluir ${exclusao.codigo}? O histórico de movimentações desse produto também será removido.` : ''"
      confirmar="Excluir"
      @cancelar="exclusao = null"
      @confirmar="confirmarExclusao"
    />
  </div>
</template>
