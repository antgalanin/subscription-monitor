#!/usr/bin/env bash
# Разрешает 443/tcp только с адресов Cloudflare (запускать при настройке сервера
# и периодически для обновления диапазонов)
set -euo pipefail

for ip in $(curl -fsS https://www.cloudflare.com/ips-v4) $(curl -fsS https://www.cloudflare.com/ips-v6); do
    sudo ufw allow proto tcp from "$ip" to any port 443 comment cloudflare
done

sudo ufw status numbered | grep -c cloudflare
