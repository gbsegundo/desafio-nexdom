import { describe, expect, it } from 'vitest'
import { dataHora, formatarMoedaDigitacao, moeda, moedaCampo, numeroDeMoeda } from './format'

describe('moeda', () => {
  it('mostra traço quando não há valor', () => {
    expect(moeda(null)).toBe('—')
    expect(moeda(undefined)).toBe('—')
    expect(moeda('')).toBe('—')
  })

  it('formata em real', () => {
    expect(moeda(2599.9)).toMatch(/2\.599,90/)
    expect(moeda('10')).toMatch(/10,00/)
  })
})

describe('dataHora', () => {
  it('mostra traço quando não há data', () => {
    expect(dataHora('')).toBe('—')
    expect(dataHora(null)).toBe('—')
  })

  it('formata data e hora', () => {
    expect(dataHora('2026-11-20T15:30:00')).toMatch(/20\/11\/2026/)
  })
})

describe('formatarMoedaDigitacao', () => {
  it('trata os dígitos como centavos', () => {
    expect(formatarMoedaDigitacao('320050')).toMatch(/3\.200,50/)
  })

  it('limpa quando não há dígitos', () => {
    expect(formatarMoedaDigitacao('')).toBe('')
    expect(formatarMoedaDigitacao('abc')).toBe('')
    expect(formatarMoedaDigitacao(null)).toBe('')
  })

  it('ignora o que passa de 14 dígitos', () => {
    expect(formatarMoedaDigitacao('123456789012345')).toMatch(/123\.456\.789\.012,34/)
  })
})

describe('numeroDeMoeda', () => {
  it('converte o texto formatado em número', () => {
    expect(numeroDeMoeda('R$ 3.200,50')).toBe(3200.5)
  })

  it('devolve nulo quando o campo está vazio', () => {
    expect(numeroDeMoeda('')).toBeNull()
    expect(numeroDeMoeda('R$')).toBeNull()
  })
})

describe('moedaCampo', () => {
  it('prepara um número da API para o campo', () => {
    expect(moedaCampo(1800)).toMatch(/1\.800,00/)
  })

  it('aceita valor da API, texto já formatado e valor ausente', () => {
    expect(moedaCampo(2599.9)).toMatch(/2\.599,90/)
    expect(moedaCampo('2599.90')).toMatch(/2\.599,90/)
    expect(moedaCampo('R$ 10,50')).toMatch(/10,50/)
    expect(moedaCampo(null)).toBe('')
    expect(moedaCampo('')).toBe('')
  })
})
