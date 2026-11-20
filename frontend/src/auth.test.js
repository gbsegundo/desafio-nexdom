import { beforeEach, describe, expect, it, vi } from 'vitest'
import { autenticado, destinoInterno, entrar, sair, usuarioLogado } from './auth'

describe('sessão', () => {
  beforeEach(() => {
    sessionStorage.clear()
    vi.restoreAllMocks()
  })

  it('entra com o usuário nexdom, ignorando maiúsculas e espaços', () => {
    expect(entrar('  Nexdom  ', 'nexdom')).toBe(true)
    expect(autenticado()).toBe(true)
    expect(usuarioLogado()).toBe('Nexdom')
  })

  it('recusa senha diferente e não abre sessão', () => {
    expect(entrar('nexdom', 'errada')).toBe(false)
    expect(autenticado()).toBe(false)
  })

  it('usa nexdom quando não há nome gravado', () => {
    expect(usuarioLogado()).toBe('nexdom')
  })

  it('encerra a sessão', () => {
    entrar('nexdom', 'nexdom')
    sair()
    expect(autenticado()).toBe(false)
    expect(usuarioLogado()).toBe('nexdom')
  })

  it('trata falha de leitura do navegador como sessão ausente', () => {
    vi.spyOn(Storage.prototype, 'getItem').mockImplementation(() => {
      throw new Error('bloqueado')
    })
    expect(autenticado()).toBe(false)
    expect(usuarioLogado()).toBe('nexdom')
  })

  it('trata falha de gravação como login recusado', () => {
    vi.spyOn(Storage.prototype, 'setItem').mockImplementation(() => {
      throw new Error('bloqueado')
    })
    expect(entrar('nexdom', 'nexdom')).toBe(false)
  })

  it('ignora falha ao sair', () => {
    vi.spyOn(Storage.prototype, 'removeItem').mockImplementation(() => {
      throw new Error('bloqueado')
    })
    expect(() => sair()).not.toThrow()
  })
})

describe('destinoInterno', () => {
  it('aceita caminhos internos', () => {
    expect(destinoInterno('/produtos')).toBe('/produtos')
    expect(destinoInterno('/consulta?tipo=ELETRONICO')).toBe('/consulta?tipo=ELETRONICO')
  })

  it('manda para a home quando o destino não é seguro', () => {
    expect(destinoInterno(null)).toBe('/')
    expect(destinoInterno('https://exemplo.test')).toBe('/')
    expect(destinoInterno('//exemplo.test')).toBe('/')
    expect(destinoInterno('/login')).toBe('/')
    expect(destinoInterno('/login?x=1')).toBe('/')
  })
})
