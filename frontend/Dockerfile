# Build stage
FROM node:24-alpine AS build
WORKDIR /app
COPY package.json package-lock.json ./
RUN npm ci --no-fund --no-audit
COPY . .
RUN npm run build

# Runtime stage
FROM caddy:2-alpine
COPY Caddyfile /etc/caddy/Caddyfile
COPY upstream.caddy /etc/caddy/upstream.caddy
COPY --from=build /app/dist /srv
EXPOSE 80
