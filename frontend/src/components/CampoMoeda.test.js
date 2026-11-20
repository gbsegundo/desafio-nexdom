import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'
import CampoMoeda from './CampoMoeda.vue'

describe('CampoMoeda', () => {
  it('formata o valor em real conforme a digitação', async () => {
    const wrapper = mount(CampoMoeda, { props: { modelValue: '', obrigatorio: true } })
    const campo = wrapper.get('input')

    await campo.setValue('320050')

    expect(campo.element.value).toMatch(/3\.200,50/)
    expect(wrapper.emitted('update:modelValue').at(-1)).toEqual([campo.element.value])
    expect(campo.attributes('placeholder')).toBe('R$ 0,00')
    expect(campo.attributes('required')).toBeDefined()
  })

  it('limpa o campo quando a digitação não tem número', async () => {
    const wrapper = mount(CampoMoeda, { props: { modelValue: 'R$ 1,00', invalido: true } })

    await wrapper.get('input').setValue('abc')

    expect(wrapper.get('input').element.value).toBe('')
    expect(wrapper.get('input').attributes('aria-invalid')).toBe('true')
  })
})
