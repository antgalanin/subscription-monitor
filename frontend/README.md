# Subscription Monitor — веб-клиент

Одностраничное приложение на Vue 3 (Composition API) с Element Plus. Общается с сервером только через REST API `/api/*`.

```bash
npm install
npm run dev        # dev server на :5173, прокси /api -> localhost:8080
npm test           # Vitest
npm run build      # прод-сборка в dist/
```

В контейнере статика раздаётся Caddy (см. `Dockerfile` и `Caddyfile`), он же проксирует `/api` на бэкенд.
