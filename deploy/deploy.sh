#!/usr/bin/env bash
# Blue-green выкатка новой версии: поднять неактивный цвет из свежего образа,
# дождаться healthcheck, переключить Caddy, погасить старый цвет.
# Миграции Flyway применяет само приложение при старте нового цвета,
# то есть до переключения трафика.
set -euo pipefail
cd "$(dirname "$0")"

TAG="${1:?Использование: deploy.sh <тег-образа>}"
COMPOSE="docker compose -f docker-compose.prod.yml"

if [[ ! -f caddy/upstream.caddy ]]; then
    echo "reverse_proxy app-blue:8080" > caddy/upstream.caddy
fi

ACTIVE=$(grep -o 'app-\(blue\|green\)' caddy/upstream.caddy | head -1 | cut -d- -f2)
if [[ "$ACTIVE" == "blue" ]]; then TARGET="green"; else TARGET="blue"; fi
echo "Активный цвет: ${ACTIVE}, разворачиваем ${TARGET}, тег образов: ${TAG}"

sed -i "s|^APP_TAG=.*|APP_TAG=${TAG}|" .env

$COMPOSE pull --quiet "app-${TARGET}" web

$COMPOSE up -d web
$COMPOSE up -d "app-${TARGET}"

echo "Ожидание готовности app-${TARGET}..."
for _ in $(seq 1 60); do
    CID=$($COMPOSE ps -q "app-${TARGET}")
    STATUS=$(docker inspect --format '{{.State.Health.Status}}' "$CID" 2>/dev/null || echo starting)
    [[ "$STATUS" == "healthy" ]] && break
    if [[ "$STATUS" == "unhealthy" ]]; then
        echo "app-${TARGET} не прошёл healthcheck, трафик остаётся на app-${ACTIVE}" >&2
        $COMPOSE logs --tail 50 "app-${TARGET}" >&2
        $COMPOSE stop "app-${TARGET}"
        exit 1
    fi
    sleep 5
done
STATUS=$(docker inspect --format '{{.State.Health.Status}}' "$($COMPOSE ps -q "app-${TARGET}")" 2>/dev/null || echo starting)
if [[ "$STATUS" != "healthy" ]]; then
    echo "app-${TARGET} не стал healthy за отведённое время, трафик остаётся на app-${ACTIVE}" >&2
    $COMPOSE stop "app-${TARGET}"
    exit 1
fi

./switch.sh "$TARGET"
echo "${ACTIVE} ${TARGET} ${TAG}" > .last_deploy

echo "Выкатка завершена: активен app-${TARGET}. Старый цвет app-${ACTIVE} будет погашен после smoke-теста (finish.sh)."
