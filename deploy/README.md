# Развёртывание на VPS

Каталог содержит всё, что живёт на сервере: прод-компоуз с blue-green схемой, конфигурацию Caddy и скрипты выкатки. Workflow [`deploy.yml`](../.github/workflows/deploy.yml) синхронизирует этот каталог на сервер при каждом релизе, поэтому править файлы нужно в репозитории, а не на сервере. Локальные для сервера файлы (`.env`, `certs/`, `caddy/upstream.caddy`, `.last_deploy`) в git не попадают и при синхронизации не затираются.

## Подготовка сервера (один раз)

Требования: Ubuntu 22.04+, 2 ГБ памяти минимум (комфортно 4 ГБ). Образы собирает CI — серверу остаётся только запускать их.

```bash
# вход по ключу вместо пароля, запрет root-входа
adduser deploy && usermod -aG sudo deploy
mkdir -p /home/deploy/.ssh && cp ~/.ssh/authorized_keys /home/deploy/.ssh/ && chown -R deploy:deploy /home/deploy/.ssh
sed -i 's/^#\?PermitRootLogin.*/PermitRootLogin no/; s/^#\?PasswordAuthentication.*/PasswordAuthentication no/' /etc/ssh/sshd_config
systemctl restart ssh

# docker
curl -fsSL https://get.docker.com | sh
usermod -aG docker deploy

# файрвол: ssh открыт, 443 — только с адресов Cloudflare (после копирования deploy/)
ufw allow OpenSSH
ufw enable
```

## Cloudflare (один раз)

1. Домен добавлен в Cloudflare, A-запись на IP сервера, **оранжевое облако включено** (адрес сервера скрыт).
2. SSL/TLS → режим **Full (strict)**.
3. SSL/TLS → Origin Server → Create Certificate (Origin CA, срок 15 лет). Приватный ключ показывается один раз — сохранить в `certs/origin-key.pem`, сертификат в `certs/origin.pem`.
4. SSL/TLS → Origin Server → **Authenticated Origin Pulls** включить; клиентский сертификат Cloudflare сохранить в `certs/authenticated_origin_pull_ca.pem` (скачивается с developers.cloudflare.com, «Cloudflare Origin Pull CA»).
5. На сервере выполнить `./cloudflare-allowlist.sh` — откроет 443 только для диапазонов Cloudflare.

Из России сайт за оранжевым облаком открывается нестабильно — проверять через VPN, это осознанный размен (см. план миграции).

## Первый запуск (один раз)

```bash
mkdir -p ~/subscription-monitor/deploy
# скопировать каталог deploy/ из репозитория (или git clone и cp)
cd ~/subscription-monitor/deploy
cp .env.example .env && nano .env          # пароли и домен
mkdir -p certs                             # положить три файла сертификатов (см. выше)
echo 'reverse_proxy app-blue:8080' > caddy/upstream.caddy
docker login ghcr.io                       # если пакеты приватные; для публичных не нужен
docker compose -f docker-compose.prod.yml up -d postgres web app-blue
```

Проверка: `docker compose -f docker-compose.prod.yml ps` — все сервисы healthy, сайт открывается по домену.

## Секреты GitHub Actions (один раз)

| Имя | Тип | Значение |
|-----|-----|----------|
| `SSH_HOST` | secret | IP или хост сервера |
| `SSH_USER` | secret | пользователь для деплоя (`deploy`) |
| `SSH_KEY` | secret | приватный ключ ed25519, парный ключ — в `authorized_keys` на сервере |
| `DOMAIN` | variable | домен сайта, для smoke-теста |

## Как проходит выкатка

`git tag v1.2.3 && git push --tags` запускает цепочку: тесты → сборка образов с тегом версии в GHCR → деплой.

Шаг деплоя на сервере (`deploy.sh`):

1. определяет активный цвет по `caddy/upstream.caddy`;
2. подтягивает свежие образы, при необходимости обновляет `web` (короткий перезапуск Caddy);
3. поднимает неактивный цвет — приложение на старте накатывает миграции Flyway, то есть схема обновляется **до** переключения трафика;
4. ждёт `healthy` от Actuator; если новый цвет не поднялся — трафик остаётся на старом, выкатка падает;
5. `switch.sh` переключает Caddy без разрыва соединений.

Дальше workflow прогоняет smoke-тест по домену: `/` должен отдать 200, `/api/auth/me` — 401. Успех → `finish.sh` гасит старый цвет. Провал → `rollback.sh` возвращает прокси на старый цвет и гасит новый.

Ручное переключение в любой момент: `./switch.sh blue|green`.

## Замер простоя для отчёта

Во время выкатки в отдельном терминале:

```bash
while true; do
  curl -o /dev/null -s -w "%{http_code} %{time_total}\n" https://ДОМЕН/api/auth/me
  sleep 0.2
done | tee downtime.log
```

Сначала прогнать при «обычном» обновлении (`docker compose up -d app-blue` с новым тегом без blue-green) — будет пачка ошибок на время рестарта Spring. Потом при штатной выкатке через тег — непрерывные `401`. Доля неуспешных ответов и есть число для отчёта.
