<script setup>
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import Alert from '../components/Alert.vue'
import { destinoInterno, entrar } from '../auth'

const route = useRoute()
const router = useRouter()
const usuario = ref('nexdom')
const senha = ref('nexdom')
const erro = ref('')
const mostrarSenha = ref(false)
const campoSenha = ref(null)

const recursos = [
  'Cadastro de produtos com saldo atualizado',
  'Entradas e saídas com controle de quantidade',
  'Lucro calculado em cada saída',
]

function enviar() {
  erro.value = ''
  if (!entrar(usuario.value, senha.value)) {
    erro.value = 'Não foi possível entrar. Use o usuário nexdom e a senha nexdom.'
    campoSenha.value?.focus()
    campoSenha.value?.select()
    return
  }
  router.push(destinoInterno(route.query.redirect))
}
</script>

<template>
  <main class="login">
    <section class="apresentacao">
      <img class="login-logo" src="/logo-nexdom.svg" alt="Nexdom Healthtech" />
      <div class="apresentacao-texto">
        <p class="eyebrow">Gestão de estoque</p>
        <h1>Operação clara, do saldo ao lucro.</h1>
        <p class="lede">
          Um só lugar para acompanhar produtos, movimentações e o resultado de cada saída.
        </p>
        <ul class="recursos">
          <li v-for="item in recursos" :key="item">{{ item }}</li>
        </ul>
      </div>
      <p class="assinatura">Nexdom Healthtech</p>
    </section>

    <section class="acesso">
      <form class="painel" @submit.prevent="enviar">
        <header>
          <p class="eyebrow">Acesso</p>
          <h2>Entrar</h2>
          <p class="lede">Os campos já estão preenchidos. Confira e continue.</p>
        </header>

        <div class="credenciais" aria-label="Credenciais de acesso">
          <p><span>Usuário</span><strong>nexdom</strong></p>
          <p><span>Senha</span><strong>nexdom</strong></p>
        </div>

        <Alert :mensagem="erro" />

        <label class="field">
          <span class="field-label">Usuário</span>
          <input
            v-model="usuario"
            name="usuario"
            autocomplete="username"
            autocapitalize="none"
            spellcheck="false"
            required
          />
        </label>

        <div class="field">
          <label class="field-label" for="senha">Senha</label>
          <span class="senha-campo">
            <input
              id="senha"
              ref="campoSenha"
              v-model="senha"
              name="senha"
              :type="mostrarSenha ? 'text' : 'password'"
              autocomplete="current-password"
              :aria-invalid="erro ? 'true' : undefined"
              required
            />
            <button class="revelar" type="button" @click="mostrarSenha = !mostrarSenha">
              {{ mostrarSenha ? 'Ocultar' : 'Mostrar' }}
            </button>
          </span>
        </div>

        <button class="primary entrar" type="submit">Entrar</button>
      </form>
    </section>
  </main>
</template>

<style scoped>
.login {
  min-height: 100vh;
  display: grid;
  grid-template-columns: minmax(280px, 0.7fr) minmax(0, 1.3fr);
}

.apresentacao {
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  gap: 36px;
  padding: 40px 32px;
  background:
    radial-gradient(circle at 100% 100%, rgba(108, 202, 152, 0.28), transparent 42%),
    var(--white);
  border-right: 1px solid var(--line);
}

.login-logo { height: 42px; width: auto; }

.apresentacao h1 {
  margin: 0;
  max-width: 12ch;
  font-size: clamp(2.4rem, 4vw, 3.4rem);
  font-weight: 500;
  letter-spacing: -1.6px;
  line-height: 1.02;
}

.apresentacao .lede {
  max-width: 36ch;
  margin: 16px 0 0;
  color: var(--muted);
  font-size: 1.05rem;
  line-height: 1.5;
}

.recursos {
  margin: 28px 0 0;
  padding: 0;
  list-style: none;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.recursos li {
  display: flex;
  align-items: center;
  gap: 12px;
  color: var(--ink);
}

.recursos li::before {
  content: "";
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: var(--mint);
  flex: 0 0 10px;
}

.assinatura {
  margin: 0;
  color: var(--muted);
  font-size: 0.92rem;
}

.acesso {
  display: grid;
  place-items: center;
  padding: 32px 24px;
  background:
    radial-gradient(ellipse at top, rgba(108, 202, 152, 0.16), transparent 46%),
    var(--paper);
}

.painel {
  width: min(680px, 100%);
  display: flex;
  flex-direction: column;
  gap: 22px;
  background: var(--white);
  border: 1px solid var(--line);
  border-radius: 22px;
  padding: 40px 36px;
  box-shadow: var(--shadow);
}

.painel h2 {
  margin: 0;
  font-size: 2.6rem;
  font-weight: 500;
  letter-spacing: -1.2px;
}

.painel .lede {
  margin: 10px 0 0;
  color: var(--muted);
  font-size: 1.08rem;
  line-height: 1.5;
}

.credenciais {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 14px;
}

.credenciais p {
  margin: 0;
  padding: 16px 18px;
  border-radius: 14px;
  background: var(--mint-soft);
}

.credenciais span {
  display: block;
  margin-bottom: 4px;
  color: var(--muted);
  font-size: 0.78rem;
  font-weight: 600;
  letter-spacing: 0.06em;
  text-transform: uppercase;
}

.credenciais strong { font-size: 1.15rem; font-weight: 600; }

.painel .field { display: flex; flex-direction: column; gap: 10px; }

.painel input { height: 64px; font-size: 1.05rem; }

.senha-campo { position: relative; display: block; }

.senha-campo input { width: 100%; padding-right: 108px; }

.revelar {
  position: absolute;
  top: 12px;
  right: 10px;
  height: 40px;
  min-height: 40px;
  padding: 0 14px;
  border-radius: 8px;
  background: transparent;
  color: var(--muted);
  font-size: 0.92rem;
  font-weight: 600;
}

.revelar:hover { background: var(--mint-soft); color: var(--ink); }

.entrar { width: 100%; min-height: 56px; margin-top: 6px; font-size: 1.05rem; }

@media (max-width: 860px) {
  .login { grid-template-columns: 1fr; }
  .apresentacao { gap: 20px; padding: 28px 20px 8px; border-right: 0; }
  .apresentacao h1 { max-width: none; font-size: 2rem; }
  .recursos, .assinatura { display: none; }
  .acesso { padding: 24px 16px 36px; }
  .painel { width: 100%; padding: 28px 22px; }
}
</style>
