import client from './client'

export const authApi = {
  login: (data) => client.post('/auth/login', data),
  register: (data) => client.post('/auth/register', data),
  logout: () => client.post('/auth/logout'),
  me: () => client.get('/auth/me'),
  updateEmail: (userId, email) => client.put(`/auth/${userId}/email`, { email }),
  changePassword: (userId, data) => client.post(`/auth/${userId}/change-password`, data)
}

export const subscriptionsApi = {
  list: () => client.get('/subscriptions'),
  create: (data) => client.post('/subscriptions', data),
  updateWithPayment: (id, data) => client.put(`/subscriptions/${id}/with-payment`, data),
  remove: (id) => client.delete(`/subscriptions/${id}`)
}

export const paymentsApi = {
  list: () => client.get('/payments'),
  create: (data) => client.post('/payments', data)
}

export const categoriesApi = {
  list: () => client.get('/categories'),
  create: (data) => client.post('/categories', data),
  update: (id, data) => client.put(`/categories/${id}`, data),
  remove: (id) => client.delete(`/categories/${id}`)
}

export const notificationsApi = {
  myReceived: () => client.get('/notifications/my/received'),
  remove: (id) => client.delete(`/notifications/${id}`)
}

export const analyticsApi = {
  myStatistics: () => client.get('/analytics/my-statistics'),
  myUpcomingPayments: () => client.get('/analytics/my-upcoming-payments'),
  myCategoryStatistics: () => client.get('/analytics/my-category-statistics')
}

export const usersApi = {
  list: () => client.get('/users'),
  update: (id, data) => client.put(`/users/${id}`, data),
  remove: (id) => client.delete(`/users/${id}`)
}
