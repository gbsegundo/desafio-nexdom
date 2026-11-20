<script setup>
import { formatarMoedaDigitacao } from '../utils/format'

const model = defineModel({ type: String, default: '' })

defineProps({
  invalido: { type: Boolean, default: false },
  obrigatorio: { type: Boolean, default: false },
})

function aoDigitar(evento) {
  const campo = evento.target
  const formatado = formatarMoedaDigitacao(campo.value)
  model.value = formatado
  campo.value = formatado
  const fim = formatado.length
  campo.setSelectionRange(fim, fim)
}
</script>

<template>
  <input
    :value="model"
    type="text"
    inputmode="numeric"
    autocomplete="off"
    placeholder="R$ 0,00"
    :required="obrigatorio"
    :aria-invalid="invalido ? 'true' : undefined"
    @input="aoDigitar"
  />
</template>
