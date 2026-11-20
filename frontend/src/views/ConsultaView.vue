<script setup>
import { computed, onMounted, ref } from 'vue'
import Alert from '../components/Alert.vue'
import { api } from '../api/client'
import { TIPOS_PRODUTO, labelTipo } from '../api/labels'

const tipo = ref('ELETRONICO')
const produtos = ref([])
const erro = ref('')
const consultando = ref(false)

const totalSaida = computed(() => produtos.value.reduce((total, item) => total + Number(item.quantidadeSaida || 0), 0))
const totalDisponivel = computed(() => produtos.value.reduce((total, item) => total + Number(item.quantidadeDisponivel || 0), 0))

async function consultar(novoTipo = tipo.value) {
  tipo.value = novoTipo
  erro.value = ''
  consultando.value = true
  try {
    produtos.value = await api(`/consultas/produtos?tipo=${tipo.value}`)
  } catch (error) {
    erro.value = error.message
    produtos.value = []
  } finally {
    consultando.value = false
  }
}

onMounted(() => consultar())
</script>

<template>
  <div class="stack">
    <Alert :mensagem="erro" />

    <section class="panel">
      <div class="panel-head">
        <div>
          <h2>{{ labelTipo(tipo) }}</h2>
          <p>Selecione o tipo para atualizar a consulta.</p>
        </div>
      </div>
      <div class="chips" role="group" aria-label="Tipo de produto">
        <button
          v-for="item in TIPOS_PRODUTO"
          :key="item.value"
          type="button"
          class="chip"
          :class="{ on: tipo === item.value }"
          :disabled="consultando"
          @click="consultar(item.value)"
        >
          {{ item.label }}
        </button>
      </div>

      <div class="metrics" style="margin-top: 18px">
        <article class="metric">
          <span>Quantidade de saída</span>
          <strong class="saida">{{ totalSaida }}</strong>
        </article>
        <article class="metric">
          <span>Quantidade disponível</span>
          <strong>{{ totalDisponivel }}</strong>
        </article>
      </div>

      <div class="table-wrap" style="margin-top: 8px">
        <table>
          <thead>
            <tr>
              <th>Código</th>
              <th>Descrição</th>
              <th class="num">Saída</th>
              <th class="num">Disponível</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="consultando">
              <td colspan="4" class="empty">Consultando...</td>
            </tr>
            <tr v-else-if="!produtos.length">
              <td colspan="4" class="empty">Nenhum produto desse tipo.</td>
            </tr>
            <tr v-for="produto in produtos" :key="produto.id">
              <td class="code">{{ produto.codigo }}</td>
              <td>{{ produto.descricao }}</td>
              <td class="num">{{ produto.quantidadeSaida }}</td>
              <td class="num"><span class="badge" :class="{ zero: produto.quantidadeDisponivel === 0 }">{{ produto.quantidadeDisponivel }}</span></td>
            </tr>
          </tbody>
        </table>
      </div>
    </section>
  </div>
</template>
