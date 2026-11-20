<script setup>
defineProps({
  aberto: { type: Boolean, default: false },
  titulo: { type: String, default: '' },
  texto: { type: String, default: '' },
  confirmar: { type: String, default: 'Confirmar' },
})

const emit = defineEmits(['confirmar', 'cancelar'])
</script>

<template>
  <div v-if="aberto" class="modal-back" @click.self="emit('cancelar')">
    <div class="modal" role="dialog" aria-modal="true" :aria-label="titulo">
      <h2>{{ titulo }}</h2>
      <p>{{ texto }}</p>
      <div class="actions">
        <button class="primary" type="button" @click="emit('confirmar')">{{ confirmar }}</button>
        <button class="ghost" type="button" @click="emit('cancelar')">Cancelar</button>
      </div>
    </div>
  </div>
</template>
