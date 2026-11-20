const moedaFormatador = new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' })
const dataFormatador = new Intl.DateTimeFormat('pt-BR', { dateStyle: 'short', timeStyle: 'short' })

export function moeda(valor) {
  if (valor === null || valor === undefined || valor === '') {
    return '—'
  }
  return moedaFormatador.format(Number(valor))
}

export function dataHora(valor) {
  if (!valor) {
    return '—'
  }
  return dataFormatador.format(new Date(valor))
}

export function formatarMoedaDigitacao(texto) {
  const digitos = String(texto ?? '').replace(/\D/g, '').slice(0, 14)
  if (!digitos) {
    return ''
  }
  return moeda(Number(digitos) / 100)
}

export function numeroDeMoeda(texto) {
  const digitos = String(texto ?? '').replace(/\D/g, '')
  if (!digitos) {
    return null
  }
  return Number(digitos) / 100
}

export function moedaCampo(valor) {
  if (valor === null || valor === undefined || valor === '') {
    return ''
  }
  const numero = Number(valor)
  if (Number.isNaN(numero)) {
    return formatarMoedaDigitacao(valor)
  }
  return formatarMoedaDigitacao(String(Math.round(numero * 100)))
}
