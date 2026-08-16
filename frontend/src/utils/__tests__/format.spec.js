import { describe, it, expect } from 'vitest'
import {
  formatMoney,
  formatDate,
  formatDateTime,
  categoryTypeLabel,
  notificationTypeLabel,
  userRoleLabel,
  errorMessage
} from '../format'

describe('formatMoney', () => {
  it('форматирует сумму с символом валюты', () => {
    expect(formatMoney(1000, 'RUB')).toContain('₽')
    expect(formatMoney(1000, 'USD')).toContain('$')
    expect(formatMoney(1000, 'EUR')).toContain('€')
  })

  it('выводит два знака после запятой', () => {
    expect(formatMoney(999.5, 'RUB')).toContain('50')
  })

  it('возвращает прочерк для пустых значений', () => {
    expect(formatMoney(null, 'RUB')).toBe('—')
    expect(formatMoney(undefined, 'RUB')).toBe('—')
  })

  it('оставляет код неизвестной валюты как есть', () => {
    expect(formatMoney(10, 'GBP')).toContain('GBP')
  })
})

describe('formatDate', () => {
  it('форматирует дату в русской локали', () => {
    expect(formatDate('2026-08-16')).toBe('16.08.2026')
  })

  it('возвращает прочерк для пустого значения', () => {
    expect(formatDate(null)).toBe('—')
    expect(formatDate('')).toBe('—')
  })
})

describe('formatDateTime', () => {
  it('содержит дату и время', () => {
    const result = formatDateTime('2026-08-16T14:30:00')
    expect(result).toContain('16.08.2026')
    expect(result).toContain('14:30')
  })
})

describe('подписи перечислений', () => {
  it('переводит типы категорий', () => {
    expect(categoryTypeLabel('SYSTEM')).toBe('Системная')
    expect(categoryTypeLabel('CUSTOM')).toBe('Пользовательская')
    expect(categoryTypeLabel('LEGACY')).toBe('Устаревшая')
  })

  it('переводит типы уведомлений', () => {
    expect(notificationTypeLabel('UPCOMING_PAYMENT')).toBe('Предстоящий платёж')
    expect(notificationTypeLabel('PAYMENT_SUCCESSFUL')).toBe('Платёж выполнен')
  })

  it('переводит роли', () => {
    expect(userRoleLabel('ADMIN')).toBe('Администратор')
    expect(userRoleLabel('USER')).toBe('Пользователь')
  })

  it('возвращает исходное значение для неизвестного кода', () => {
    expect(categoryTypeLabel('UNKNOWN')).toBe('UNKNOWN')
  })
})

describe('errorMessage', () => {
  it('берёт сообщение из ответа сервера', () => {
    const error = { response: { data: { message: 'Категория не найдена' } } }
    expect(errorMessage(error)).toBe('Категория не найдена')
  })

  it('возвращает запасной текст без ответа сервера', () => {
    expect(errorMessage(new Error('network'), 'Ошибка сети')).toBe('Ошибка сети')
  })
})
