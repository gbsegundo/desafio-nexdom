import { createRouter, createWebHistory } from 'vue-router'
import { autenticado } from '../auth'
import LoginView from '../views/LoginView.vue'
import DashboardView from '../views/DashboardView.vue'
import ProdutosView from '../views/ProdutosView.vue'
import MovimentosView from '../views/MovimentosView.vue'
import ConsultaView from '../views/ConsultaView.vue'
import LucroView from '../views/LucroView.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/login',
      component: LoginView,
      meta: { publica: true },
    },
    {
      path: '/',
      component: DashboardView,
      meta: {
        eyebrow: 'Visão geral',
        title: 'Dashboard',
        lede: 'O resumo do estoque, das movimentações e do lucro, no mesmo lugar.',
      },
    },
    {
      path: '/produtos',
      component: ProdutosView,
      meta: {
        eyebrow: 'Cadastro',
        title: 'Produtos em estoque',
        lede: 'Inclua, atualize e acompanhe o saldo de forma simples, sem burocracia.',
      },
    },
    {
      path: '/movimentos',
      component: MovimentosView,
      meta: {
        eyebrow: 'Entrada e saída',
        title: 'Movimentar estoque',
        lede: 'A saída só é concluída quando existe saldo suficiente para a quantidade pedida.',
      },
    },
    {
      path: '/consulta',
      component: ConsultaView,
      meta: {
        eyebrow: 'Saldo',
        title: 'Consulta por tipo',
        lede: 'Veja, para cada tipo, quanto já saiu e quanto ainda está disponível.',
      },
    },
    {
      path: '/lucro',
      component: LucroView,
      meta: {
        eyebrow: 'Resultado',
        title: 'Lucro por produto',
        lede: 'O lucro de cada saída é o valor de venda menos o valor do fornecedor, multiplicado pela quantidade.',
      },
    },
  ],
})

router.beforeEach((to) => {
  if (to.meta.publica) {
    if (autenticado()) return { path: '/' }
    return true
  }
  if (!autenticado()) {
    return { path: '/login', query: { redirect: to.fullPath } }
  }
  return true
})

export default router
