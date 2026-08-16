const CURRENCY_SYMBOLS = { RUB: '₽', USD: '$', EUR: '€' }

const CATEGORY_TYPES = { SYSTEM: 'Системная', CUSTOM: 'Пользовательская', LEGACY: 'Устаревшая' }

const NOTIFICATION_TYPES = { UPCOMING_PAYMENT: 'Предстоящий платёж', PAYMENT_SUCCESSFUL: 'Платёж выполнен' }

const USER_ROLES = { USER: 'Пользователь', ADMIN: 'Администратор' }

export function formatMoney(value, currency) {
  if (value === null || value === undefined) return '—'
  const amount = Number(value).toLocaleString('ru-RU', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
  })
  return `${amount} ${CURRENCY_SYMBOLS[currency] ?? currency ?? ''}`.trim()
}

export function formatDate(value) {
  if (!value) return '—'
  const date = new Date(value)
  return date.toLocaleDateString('ru-RU')
}

export function formatDateTime(value) {
  if (!value) return '—'
  const date = new Date(value)
  return `${date.toLocaleDateString('ru-RU')} ${date.toLocaleTimeString('ru-RU', { hour: '2-digit', minute: '2-digit' })}`
}

export function categoryTypeLabel(type) {
  return CATEGORY_TYPES[type] ?? type
}

export function notificationTypeLabel(type) {
  return NOTIFICATION_TYPES[type] ?? type
}

export function userRoleLabel(role) {
  return USER_ROLES[role] ?? role
}

export function errorMessage(error, fallback = 'Произошла ошибка') {
  return error?.response?.data?.message || fallback
}
