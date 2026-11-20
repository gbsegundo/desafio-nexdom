import { describe, expect, it } from 'vitest'
import { labelMovimento, labelTipo } from './labels'

describe('rótulos', () => {
  it('traduz os tipos conhecidos', () => {
    expect(labelTipo('ELETRONICO')).toBe('Eletrônico')
    expect(labelTipo('ELETRODOMESTICO')).toBe('Eletrodoméstico')
    expect(labelTipo('MOVEL')).toBe('Móvel')
    expect(labelMovimento('ENTRADA')).toBe('Entrada')
    expect(labelMovimento('SAIDA')).toBe('Saída')
  })

  it('devolve o próprio valor quando não reconhece', () => {
    expect(labelTipo('OUTRO')).toBe('OUTRO')
    expect(labelMovimento('AJUSTE')).toBe('AJUSTE')
  })
})
