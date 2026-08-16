#!/usr/bin/env bash
# Завершение выкатки после успешного smoke-теста: погасить старый цвет
set -euo pipefail
cd "$(dirname "$0")"

[[ -f .last_deploy ]] || { echo "Нет файла .last_deploy — нечего завершать" >&2; exit 1; }
read -r OLD NEW TAG < .last_deploy

docker compose -f docker-compose.prod.yml stop "app-${OLD}"
rm -f .last_deploy
echo "Выкатка ${TAG} завершена: работает app-${NEW}, app-${OLD} остановлен"
