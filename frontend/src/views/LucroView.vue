<script setup>
import { computed, onMounted, ref } from 'vue'
import Alert from '../components/Alert.vue'
import { api } from '../api/client'
import { labelTipo } from '../api/labels'
import { moeda } from '../utils/format'

const linhas = ref([])
const erro = ref('')
const carregando = ref(true)

const totalLucro = computed(() =>
  linhas.value.reduce((total, linha) => total + Number(linha.totalLucro || 0), 0),
)
const totalSaida = computed(() =>
  linhas.value.reduce((total, linha) => total + Number(linha.quantidadeTotalSaida || 0), 0),
)

onMounted(async () => {
  try {
    linhas.value = await api('/consultas/lucro')
  } catch (error) {
    erro.value = error.message
  } finally {
    carregando.value = false
  }
})
</script>

<template>
  <div class="stack">
    <Alert :mensagem="erro" />
    <div class="metrics">
      <article class="metric">
        <span>Quantidade total de saída</span>
        <strong>{{ totalSaida }}</strong>
      </article>
      <article class="metric">
        <span>Lucro acumulado</span>
        <strong :class="{ negativo: totalLucro < 0 }">{{ moeda(totalLucro) }}</strong>
      </article>
    </div>

    <section class="panel">
      <div class="panel-head">
        <div>
          <h2>Resultado por produto</h2>
          <p>Entradas não entram nessa conta. Produtos sem saída aparecem com lucro zero.</p>
        </div>
      </div>
      <div class="table-wrap">
        <table>
          <thead>
            <tr>
              <th>Código</th>
              <th>Descrição</th>
              <th>Tipo</th>
              <th class="num">Saída</th>
              <th class="num">Lucro</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="carregando">
              <td colspan="5" class="empty">Calculando lucro...</td>
            </tr>
            <tr v-else-if="!linhas.length">
              <td colspan="5" class="empty">Nenhum produto para calcular.</td>
            </tr>
            <tr v-for="linha in linhas" :key="linha.id">
              <td class="code">{{ linha.codigo }}</td>
              <td>{{ linha.descricao }}</td>
              <td>{{ labelTipo(linha.tipoProduto) }}</td>
              <td class="num">{{ linha.quantidadeTotalSaida }}</td>
              <td class="num" :class="{ negativo: Number(linha.totalLucro) < 0 }">{{ moeda(linha.totalLucro) }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </section>
  </div>
</template>
