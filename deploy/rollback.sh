#!/usr/bin/env bash
# Откат после провала smoke-теста: вернуть прокси на старый цвет, погасить новый
set -euo pipefail
cd "$(dirname "$0")"

[[ -f .last_deploy ]] || { echo "Нет файла .last_deploy — нечего откатывать" >&2; exit 1; }
read -r OLD NEW TAG < .last_deploy

./switch.sh "$OLD"
docker compose -f docker-compose.prod.yml stop "app-${NEW}"
rm -f .last_deploy
echo "Откат выполнен: трафик возвращён на app-${OLD}, app-${NEW} остановлен"
