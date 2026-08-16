#!/usr/bin/env bash
# Переключение реверс-прокси на указанный цвет без разрыва соединений
set -euo pipefail
cd "$(dirname "$0")"

TARGET="${1:?Использование: switch.sh blue|green}"
[[ "$TARGET" == "blue" || "$TARGET" == "green" ]] || { echo "Цвет должен быть blue или green" >&2; exit 1; }

echo "reverse_proxy app-${TARGET}:8080" > caddy/upstream.caddy
docker compose -f docker-compose.prod.yml exec -T web caddy reload --config /etc/caddy/Caddyfile
echo "Прокси переключён на app-${TARGET}"
