<script setup>
import { computed, onMounted, ref } from 'vue'
import Alert from '../components/Alert.vue'
import { api } from '../api/client'
import { TIPOS_PRODUTO, labelMovimento, labelTipo } from '../api/labels'
import { dataHora, moeda } from '../utils/format'

const produtos = ref([])
const movimentos = ref([])
const lucros = ref([])
const erro = ref('')
const carregando = ref(true)

const cores = {
  ELETRONICO: '#6cca98',
  ELETRODOMESTICO: '#06262d',
  MOVEL: '#7f989c',
}

const totalProdutos = computed(() => produtos.value.length)
const totalEstoque = computed(() => produtos.value.reduce((total, item) => total + Number(item.quantidadeEstoque || 0), 0))
const totalLucro = computed(() => lucros.value.reduce((total, item) => total + Number(item.totalLucro || 0), 0))
const totalSaida = computed(() => lucros.value.reduce((total, item) => total + Number(item.quantidadeTotalSaida || 0), 0))
const totalEntrada = computed(() => movimentos.value
  .filter((item) => item.tipoMovimentacao === 'ENTRADA')
  .reduce((total, item) => total + Number(item.quantidade || 0), 0))
const semSaldo = computed(() => produtos.value.filter((item) => Number(item.quantidadeEstoque) === 0).length)
const textoSemSaldo = computed(() => {
  if (semSaldo.value === 0) return 'Nenhum produto sem saldo.'
  if (semSaldo.value === 1) return '1 produto sem saldo.'
  return `${semSaldo.value} produtos sem saldo.`
})

const lucroOrdenado = computed(() =>
  [...lucros.value].sort((a, b) => Number(b.totalLucro) - Number(a.totalLucro)),
)
const maiorLucro = computed(() => Math.max(...lucroOrdenado.value.map((item) => Number(item.totalLucro) || 0), 0))

const porTipo = computed(() => TIPOS_PRODUTO.map((tipo) => {
  const doTipo = produtos.value.filter((item) => item.tipoProduto === tipo.value)
  const lucroTipo = lucros.value.filter((item) => item.tipoProduto === tipo.value)
  return {
    ...tipo,
    cor: cores[tipo.value],
    produtos: doTipo.length,
    estoque: doTipo.reduce((total, item) => total + Number(item.quantidadeEstoque || 0), 0),
    saida: lucroTipo.reduce((total, item) => total + Number(item.quantidadeTotalSaida || 0), 0),
    lucro: lucroTipo.reduce((total, item) => total + Number(item.totalLucro || 0), 0),
  }
}))

const estoquePorTipo = computed(() => porTipo.value.reduce((total, item) => total + item.estoque, 0))

const fatias = computed(() => {
  let cursor = 25
  return porTipo.value.map((item) => {
    const percent = estoquePorTipo.value ? (item.estoque / estoquePorTipo.value) * 100 : 0
    const fatia = { ...item, percent, offset: cursor }
    cursor -= percent
    return fatia
  })
})

const meses = computed(() => {
  const mapa = new Map()
  for (const movimento of movimentos.value) {
    const data = movimento.dataVenda ? new Date(movimento.dataVenda) : null
    if (!data || Number.isNaN(data.getTime())) continue
    const chave = `${data.getFullYear()}-${String(data.getMonth() + 1).padStart(2, '0')}`
    const atual = mapa.get(chave) || {
      chave,
      rotulo: data.toLocaleDateString('pt-BR', { month: 'short' }).replace('.', ''),
      entrada: 0,
      saida: 0,
    }
    if (movimento.tipoMovimentacao === 'SAIDA') atual.saida += Number(movimento.quantidade || 0)
    else atual.entrada += Number(movimento.quantidade || 0)
    mapa.set(chave, atual)
  }
  return [...mapa.values()].sort((a, b) => a.chave.localeCompare(b.chave))
})

const maiorMes = computed(() => Math.max(...meses.value.flatMap((item) => [item.entrada, item.saida]), 0))

const recentes = computed(() => movimentos.value.slice(0, 5))
const atencao = computed(() =>
  [...produtos.value].sort((a, b) => a.quantidadeEstoque - b.quantidadeEstoque).slice(0, 4),
)

function largura(valor) {
  if (!maiorLucro.value) return '0%'
  return `${Math.max((Number(valor) / maiorLucro.value) * 100, valor > 0 ? 4 : 0)}%`
}

function altura(valor) {
  if (!maiorMes.value) return '0%'
  return `${Math.max((Number(valor) / maiorMes.value) * 100, valor > 0 ? 8 : 0)}%`
}

onMounted(async () => {
  try {
    const [listaProdutos, listaMovimentos, listaLucro] = await Promise.all([
      api('/produtos'),
      api('/movimentos'),
      api('/consultas/lucro'),
    ])
    produtos.value = listaProdutos
    movimentos.value = listaMovimentos
    lucros.value = listaLucro
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

    <div class="kpi">
      <RouterLink class="metric" to="/produtos">
        <span>Produtos</span>
        <strong>{{ carregando ? '—' : totalProdutos }}</strong>
      </RouterLink>
      <RouterLink class="metric" to="/produtos">
        <span>Unidades em estoque</span>
        <strong>{{ carregando ? '—' : totalEstoque }}</strong>
      </RouterLink>
      <RouterLink class="metric" to="/consulta">
        <span>Quantidade de saída</span>
        <strong>{{ carregando ? '—' : totalSaida }}</strong>
      </RouterLink>
      <RouterLink class="metric" to="/lucro">
        <span>Lucro acumulado</span>
        <strong :class="{ negativo: totalLucro < 0 }">{{ carregando ? '—' : moeda(totalLucro) }}</strong>
      </RouterLink>
    </div>

    <section class="panel">
      <div class="panel-head">
        <div>
          <h2>Movimentação por mês</h2>
          <p>Quantidade de entrada e de saída em cada mês.</p>
        </div>
        <div class="legend">
          <span><i class="swatch entrada"></i> Entrada {{ carregando ? '' : totalEntrada }}</span>
          <span><i class="swatch saida"></i> Saída {{ carregando ? '' : totalSaida }}</span>
        </div>
      </div>
      <p v-if="carregando" class="empty">Carregando resumo...</p>
      <p v-else-if="!meses.length" class="empty">Ainda não há movimentações para montar o gráfico.</p>
      <div v-else class="months">
        <div v-for="mes in meses" :key="mes.chave" class="month">
          <div class="month-bars">
            <div class="month-bar entrada" :style="{ height: altura(mes.entrada) }" :title="`Entrada: ${mes.entrada}`"></div>
            <div class="month-bar saida" :style="{ height: altura(mes.saida) }" :title="`Saída: ${mes.saida}`"></div>
          </div>
          <span>{{ mes.rotulo }}</span>
        </div>
      </div>
    </section>

    <div class="dash-grid">
      <section class="panel">
        <div class="panel-head">
          <div>
            <h2>Lucro por produto</h2>
            <p>Valor de venda menos o valor do fornecedor, nas saídas.</p>
          </div>
        </div>
        <p v-if="carregando" class="empty">Carregando resumo...</p>
        <p v-else-if="!lucroOrdenado.length" class="empty">Nenhum produto cadastrado.</p>
        <ul v-else class="bars">
          <li v-for="item in lucroOrdenado" :key="item.id">
            <div class="bar-meta">
              <span><b>{{ item.codigo }}</b> {{ item.descricao }}</span>
              <strong :class="{ negativo: Number(item.totalLucro) < 0 }">{{ moeda(item.totalLucro) }}</strong>
            </div>
            <div class="bar-track">
              <div class="bar-fill" :style="{ width: largura(Math.max(Number(item.totalLucro), 0)) }"></div>
            </div>
          </li>
        </ul>
      </section>

      <section class="panel">
        <div class="panel-head">
          <div>
            <h2>Estoque por tipo</h2>
            <p>{{ carregando ? 'Distribuição das unidades por tipo.' : textoSemSaldo }}</p>
          </div>
        </div>
        <div v-if="!carregando" class="donut-wrap">
          <svg viewBox="0 0 42 42" class="donut" role="img" aria-label="Distribuição do estoque por tipo">
            <circle cx="21" cy="21" r="15.915" fill="none" stroke="#eef2f2" stroke-width="6" />
            <circle
              v-for="fatia in fatias"
              :key="fatia.value"
              cx="21"
              cy="21"
              r="15.915"
              fill="none"
              :stroke="fatia.cor"
              stroke-width="6"
              :stroke-dasharray="`${fatia.percent} ${100 - fatia.percent}`"
              :stroke-dashoffset="fatia.offset"
            />
          </svg>
          <div class="donut-center">
            <strong>{{ totalEstoque }}</strong>
            <span>unid.</span>
          </div>
        </div>
        <ul v-if="!carregando" class="tipo-list">
          <li v-for="tipo in porTipo" :key="tipo.value">
            <span><i class="swatch" :style="{ background: tipo.cor }"></i> {{ tipo.label }}</span>
            <strong>{{ tipo.estoque }}</strong>
          </li>
        </ul>
      </section>
    </div>

    <div class="dash-grid">
      <section class="panel">
        <div class="panel-head">
          <div>
            <h2>Últimas movimentações</h2>
            <p>As cinco mais recentes.</p>
          </div>
          <RouterLink class="ver-todas" to="/movimentos">Ver todas</RouterLink>
        </div>
        <p v-if="carregando" class="empty">Carregando resumo...</p>
        <p v-else-if="!recentes.length" class="empty">Nenhuma movimentação registrada.</p>
        <ul v-else class="feed">
          <li v-for="movimento in recentes" :key="movimento.id">
            <div>
              <span class="code">{{ movimento.produtoCodigo }}</span>
              <p>{{ movimento.produtoDescricao }}</p>
            </div>
            <span class="pill" :class="movimento.tipoMovimentacao === 'SAIDA' ? 'out' : 'in'">
              {{ labelMovimento(movimento.tipoMovimentacao) }} · {{ movimento.quantidade }}
            </span>
            <time>{{ dataHora(movimento.dataVenda) }}</time>
          </li>
        </ul>
      </section>

      <section class="panel">
        <div class="panel-head">
          <div>
            <h2>Atenção ao saldo</h2>
            <p>Produtos com menor quantidade disponível.</p>
          </div>
        </div>
        <p v-if="carregando" class="empty">Carregando resumo...</p>
        <p v-else-if="!atencao.length" class="empty">Nenhum produto cadastrado.</p>
        <ul v-else class="feed">
          <li v-for="produto in atencao" :key="produto.id">
            <div>
              <span class="code">{{ produto.codigo }}</span>
              <p>{{ labelTipo(produto.tipoProduto) }}</p>
            </div>
            <span class="badge" :class="{ zero: produto.quantidadeEstoque === 0 }">{{ produto.quantidadeEstoque }}</span>
          </li>
        </ul>
      </section>
    </div>
  </div>
</template>

<style scoped>
.legend { display: flex; gap: 14px; color: var(--muted); font-size: 0.92rem; }
.swatch { display: inline-block; width: 10px; height: 10px; border-radius: 50%; margin-right: 6px; }
.swatch.entrada { background: var(--mint); }
.swatch.saida { background: var(--ink); }

.months {
  display: flex;
  align-items: end;
  gap: 22px;
  min-height: 180px;
  padding-top: 8px;
}
.month { display: flex; flex-direction: column; align-items: center; gap: 8px; min-width: 64px; }
.month span { color: var(--muted); font-size: 0.82rem; text-transform: capitalize; }
.month-bars { display: flex; align-items: end; gap: 6px; height: 140px; }
.month-bar { width: 16px; border-radius: 8px 8px 3px 3px; }
.month-bar.entrada { background: var(--mint); }
.month-bar.saida { background: var(--ink); }

.bars { list-style: none; margin: 0; padding: 0; display: flex; flex-direction: column; gap: 14px; }
.bar-meta { display: flex; justify-content: space-between; align-items: baseline; gap: 12px; margin-bottom: 6px; }
.bar-meta span { min-width: 0; color: var(--muted); }
.bar-meta b { color: var(--ink); margin-right: 6px; }
.bar-meta strong { white-space: nowrap; }
.bar-track { height: 10px; border-radius: 999px; background: #eef2f2; overflow: hidden; }
.bar-fill { height: 100%; border-radius: inherit; background: var(--mint); }

.donut-wrap { position: relative; width: 180px; margin: 8px auto 16px; }
.donut { width: 180px; height: 180px; transform: rotate(0deg); }
.donut circle { transform-origin: center; }
.donut-center {
  position: absolute;
  inset: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}
.donut-center strong { font-size: 1.7rem; letter-spacing: -0.6px; }
.donut-center span { color: var(--muted); font-size: 0.82rem; }
.tipo-list { list-style: none; margin: 0; padding: 0; display: flex; flex-direction: column; gap: 10px; }
.tipo-list li { display: flex; justify-content: space-between; align-items: center; }

.feed { list-style: none; margin: 0; padding: 0; }
.feed li {
  display: grid;
  grid-template-columns: 1fr auto auto;
  gap: 12px;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #ece8ec;
}
.feed li:last-child { border-bottom: 0; }
.feed p { margin: 2px 0 0; color: var(--muted); }
.feed time { color: var(--muted); font-size: 0.85rem; }
.ver-todas {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  align-self: center;
  min-height: 48px;
  padding: 0 22px;
  border-radius: 10px;
  background: var(--mint);
  color: var(--ink);
  font-weight: 500;
  text-decoration: none;
  white-space: nowrap;
}
.ver-todas:hover { background: var(--mint-deep); }

@media (max-width: 720px) {
  .feed li { grid-template-columns: 1fr auto; }
  .feed time { grid-column: 1 / -1; }
}
</style>
