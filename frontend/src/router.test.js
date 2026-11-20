import { beforeEach, describe, expect, it, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import { defineComponent, h } from 'vue'

function tela(nome) {
  return defineComponent({
    name: nome,
    setup() {
      return () => h('div', nome)
    },
  })
}

vi.mock('./views/LoginView.vue', () => ({ default: tela('LoginView') }))
vi.mock('./views/DashboardView.vue', () => ({ default: tela('DashboardView') }))
vi.mock('./views/ProdutosView.vue', () => ({ default: tela('ProdutosView') }))
vi.mock('./views/MovimentosView.vue', () => ({ default: tela('MovimentosView') }))
vi.mock('./views/ConsultaView.vue', () => ({ default: tela('ConsultaView') }))
vi.mock('./views/LucroView.vue', () => ({ default: tela('LucroView') }))

const { default: router } = await import('./router/index.js')
const { entrar, sair } = await import('./auth.js')

function montar() {
  return mount({ template: '<router-view />' }, { global: { plugins: [router] } })
}

describe('roteador', () => {
  beforeEach(async () => {
    sair()
    await router.push('/login')
    await router.isReady()
  })

  it('protege uma rota interna', async () => {
    const wrapper = montar()
    await router.push('/produtos')
    await router.isReady()
    expect(router.currentRoute.value.path).toBe('/login')
    expect(router.currentRoute.value.query.redirect).toBe('/produtos')
    wrapper.unmount()
  })

  it('entra na home depois do login', async () => {
    entrar('nexdom', 'nexdom')
    const wrapper = montar()
    await router.push('/')
    await router.isReady()
    expect(router.currentRoute.value.path).toBe('/')
    wrapper.unmount()
  })

  it('desvia o login quando a sessão já existe', async () => {
    entrar('nexdom', 'nexdom')
    const wrapper = montar()
    await router.push('/produtos')
    await router.push('/login')
    await router.isReady()
    expect(router.currentRoute.value.path).toBe('/')
    wrapper.unmount()
  })
})
