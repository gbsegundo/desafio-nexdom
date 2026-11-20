export const TIPOS_PRODUTO = [
  { value: 'ELETRONICO', label: 'Eletrônico' },
  { value: 'ELETRODOMESTICO', label: 'Eletrodoméstico' },
  { value: 'MOVEL', label: 'Móvel' },
]

export const TIPOS_MOVIMENTO = [
  { value: 'ENTRADA', label: 'Entrada' },
  { value: 'SAIDA', label: 'Saída' },
]

export function labelTipo(valor) {
  return TIPOS_PRODUTO.find((tipo) => tipo.value === valor)?.label ?? valor
}

export function labelMovimento(valor) {
  return TIPOS_MOVIMENTO.find((tipo) => tipo.value === valor)?.label ?? valor
}
