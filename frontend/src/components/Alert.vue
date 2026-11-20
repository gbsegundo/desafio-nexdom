<script setup>
defineProps({
  tipo: { type: String, default: 'erro' },
  mensagem: { type: String, default: '' },
  campos: { type: Object, default: () => ({}) },
})
</script>

<template>
  <div v-if="mensagem" class="alert" :class="tipo" role="alert">
    <p>{{ mensagem }}</p>
    <ul v-if="campos && Object.keys(campos).length">
      <li v-for="(texto, campo) in campos" :key="campo">{{ texto }}</li>
    </ul>
  </div>
</template>
