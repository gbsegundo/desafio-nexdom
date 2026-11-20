const CHAVE = 'nexdom-sessao'
const CHAVE_USUARIO = 'nexdom-usuario'
const USUARIO = 'nexdom'
const SENHA = 'nexdom'

export function autenticado() {
  try {
    return sessionStorage.getItem(CHAVE) === 'ok'
  } catch {
    return false
  }
}

export function usuarioLogado() {
  try {
    return sessionStorage.getItem(CHAVE_USUARIO) || USUARIO
  } catch {
    return USUARIO
  }
}

export function entrar(usuario, senha) {
  const valido = String(usuario || '').trim().toLowerCase() === USUARIO && senha === SENHA
  if (!valido) return false
  try {
    sessionStorage.setItem(CHAVE, 'ok')
    sessionStorage.setItem(CHAVE_USUARIO, String(usuario).trim() || USUARIO)
  } catch {
    return false
  }
  return true
}

export function sair() {
  try {
    sessionStorage.removeItem(CHAVE)
    sessionStorage.removeItem(CHAVE_USUARIO)
  } catch {
    /* a sessão já não pode ser lida */
  }
}

export function destinoInterno(valor) {
  if (typeof valor !== 'string' || !valor.startsWith('/') || valor.startsWith('//') || valor.startsWith('/login')) {
    return '/'
  }
  return valor
}
