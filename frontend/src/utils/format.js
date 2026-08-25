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
  return `${amount}\u00a0${CURRENCY_SYMBOLS[currency] ?? currency ?? ''}`.trim()
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

const DAY_FORMS = ['день', 'дня', 'дней']

export function pluralize(count, forms) {
  const mod10 = count % 10
  const mod100 = count % 100
  if (mod10 === 1 && mod100 !== 11) return forms[0]
  if (mod10 >= 2 && mod10 <= 4 && (mod100 < 12 || mod100 > 14)) return forms[1]
  return forms[2]
}

export function daysUntil(value) {
  if (!value) return null
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  const target = new Date(value)
  if (Number.isNaN(target.getTime())) return null
  target.setHours(0, 0, 0, 0)
  return Math.round((target - today) / 86400000)
}

export function relativeDate(value) {
  const days = daysUntil(value)
  if (days === null) return ''
  if (days === 0) return 'сегодня'
  if (days === 1) return 'завтра'
  if (days === -1) return 'вчера'
  const count = Math.abs(days)
  const suffix = `${count} ${pluralize(count, DAY_FORMS)}`
  return days < 0 ? `просрочен на ${suffix}` : `через ${suffix}`
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
