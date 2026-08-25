import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import StatusTag from '../StatusTag.vue'

describe('StatusTag', () => {
  it('выводит подпись и класс выбранного тона', () => {
    const wrapper = mount(StatusTag, { props: { tone: 'mint', label: 'Активна' } })
    expect(wrapper.text()).toBe('Активна')
    expect(wrapper.classes()).toContain('tag--mint')
  })

  it('по умолчанию рисует точку и нейтральный тон', () => {
    const wrapper = mount(StatusTag, { props: { label: 'Позже' } })
    expect(wrapper.find('.tag__dot').exists()).toBe(true)
    expect(wrapper.classes()).toContain('tag--slate')
  })

  it('точку можно отключить', () => {
    const wrapper = mount(StatusTag, { props: { label: 'Системная', dot: false } })
    expect(wrapper.find('.tag__dot').exists()).toBe(false)
  })

  it('подпись всегда сопровождает цвет', () => {
    const wrapper = mount(StatusTag, { props: { tone: 'rose', label: 'Просрочен' } })
    expect(wrapper.text().length).toBeGreaterThan(0)
  })
})
