#!/bin/sh
set -e
mkdir -p /var/cache/apache2/proxy
chown -R daemon:daemon /var/cache/apache2
exec "$@"
