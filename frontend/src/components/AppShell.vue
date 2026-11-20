<script setup>
import { computed, onMounted, onUnmounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { sair, usuarioLogado } from '../auth'

const route = useRoute()
const router = useRouter()
const title = computed(() => route.meta.title || 'Estoque')
const eyebrow = computed(() => route.meta.eyebrow || '')
const lede = computed(() => route.meta.lede || '')
const usuario = usuarioLogado()
const inicial = usuario.charAt(0).toUpperCase()
const agora = ref(new Date())
let relogio

const momento = computed(() => {
  const data = agora.value.toLocaleDateString('pt-BR', {
    day: '2-digit',
    month: 'short',
    year: 'numeric',
  }).replace('.', '')
  const hora = agora.value.toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' })
  return `${data} · ${hora}`
})

onMounted(() => {
  relogio = setInterval(() => {
    agora.value = new Date()
  }, 1000)
})

onUnmounted(() => clearInterval(relogio))

function encerrar() {
  sair()
  router.push('/login')
}

const recolhido = ref(lerPreferencia())
const mobileAberto = ref(false)

const links = [
  {
    to: '/',
    label: 'Dashboard',
    icon: 'M4 4h7v7H4zM13 4h7v7h-7zM4 13h7v7H4zM13 13h7v7h-7z',
  },
  {
    to: '/produtos',
    label: 'Produtos',
    icon: 'M4 7.5 12 3l8 4.5v9L12 21l-8-4.5v-9Z M12 12.2 20 7.7 M12 12.2V21 M12 12.2 4 7.7',
  },
  {
    to: '/movimentos',
    label: 'Movimentações',
    icon: 'M4 8h12M13 5l3 3-3 3M20 16H8M11 13l-3 3 3 3',
  },
  {
    to: '/consulta',
    label: 'Consulta',
    icon: 'M4 6h16M4 12h10M4 18h7',
  },
  {
    to: '/lucro',
    label: 'Lucro',
    icon: 'M4 17l5-5 3 3 8-8M14 7h6v6',
  },
]

watch(recolhido, (valor) => {
  try {
    localStorage.setItem('nexdom-menu', valor ? 'recolhido' : 'aberto')
  } catch {
    /* preferência é só um detalhe visual */
  }
})

watch(() => route.path, () => {
  mobileAberto.value = false
})

function lerPreferencia() {
  try {
    return localStorage.getItem('nexdom-menu') === 'recolhido'
  } catch {
    return false
  }
}

function alternar() {
  if (window.innerWidth <= 860) {
    mobileAberto.value = !mobileAberto.value
    return
  }
  recolhido.value = !recolhido.value
}
</script>

<template>
  <div class="shell" :class="{ recolhido, 'menu-mobile': mobileAberto }">
    <header class="mobile-bar">
      <button class="menu-toggle" type="button" :aria-expanded="mobileAberto" aria-controls="menu-principal" aria-label="Abrir menu" @click="mobileAberto = !mobileAberto">
        <span></span>
        <span></span>
        <span></span>
      </button>
      <img src="/logo-nexdom.svg" alt="Nexdom Healthtech" />
    </header>

    <button v-if="mobileAberto" class="sidebar-backdrop" type="button" aria-label="Fechar menu" @click="mobileAberto = false"></button>

    <aside id="menu-principal" class="sidebar" :class="{ recolhido, aberto: mobileAberto }">
      <div class="side-top">
        <RouterLink class="brand" to="/" aria-label="Nexdom">
          <img src="/logo-nexdom.svg" alt="" />
        </RouterLink>
        <button class="recolher" type="button" :aria-expanded="!recolhido" :aria-label="recolhido ? 'Expandir menu' : 'Recolher menu'" @click="alternar">
          <svg viewBox="0 0 24 24" aria-hidden="true">
            <path d="M14 6 8 12l6 6" />
          </svg>
        </button>
      </div>

      <p class="side-label">Menu</p>
      <nav class="side-nav">
        <RouterLink v-for="link in links" :key="link.to" :to="link.to" :title="link.label">
          <svg viewBox="0 0 24 24" aria-hidden="true">
            <path :d="link.icon" />
          </svg>
          <span>{{ link.label }}</span>
        </RouterLink>
      </nav>
    </aside>

    <div class="main-column">
      <main class="workspace">
        <header class="page-head">
          <div class="intro-slot">
            <Transition name="pagina">
              <div :key="route.path" class="page-intro">
                <p v-if="eyebrow" class="eyebrow">{{ eyebrow }}</p>
                <h1>{{ title }}</h1>
                <p v-if="lede" class="lede">{{ lede }}</p>
              </div>
            </Transition>
          </div>
          <div class="sessao">
            <span class="avatar" aria-hidden="true">{{ inicial }}</span>
            <div class="sessao-dados">
              <strong>{{ usuario }}</strong>
              <time :datetime="agora.toISOString()">{{ momento }}</time>
            </div>
            <button class="sair" type="button" @click="encerrar">
              <svg viewBox="0 0 24 24" aria-hidden="true">
                <path d="M10 7V5a1 1 0 0 1 1-1h8v16h-8a1 1 0 0 1-1-1v-2" />
                <path d="M14 12H4" />
                <path d="M7 9 4 12l3 3" />
              </svg>
              <span>Sair</span>
            </button>
          </div>
        </header>
        <div class="palco">
          <RouterView v-slot="{ Component }">
            <Transition name="pagina">
              <component :is="Component" :key="route.path" />
            </Transition>
          </RouterView>
        </div>
      </main>
    </div>
  </div>
</template>
