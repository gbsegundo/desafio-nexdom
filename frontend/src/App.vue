<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import AppShell from './components/AppShell.vue'

const route = useRoute()
const publica = computed(() => route.meta.publica === true)
</script>

<template>
  <Transition name="troca" mode="out-in" appear>
    <RouterView v-if="publica" />
    <AppShell v-else />
  </Transition>
</template>
