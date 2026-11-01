#!/usr/bin/env bash
# Build the OSGi bundle and FileVault package, install both into the local
# Sling instance, then flush the dispatcher cache.
#
# The bundle goes through sling-maven-plugin. The content package goes
# through Composum's package HTTP API (sling:install only accepts bundles).
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

if [[ -f "${ROOT}/.env" ]]; then
  set -a
  # shellcheck disable=SC1091
  source "${ROOT}/.env"
  set +a
fi

if [[ -z "${SLING_ADMIN_USER:-}" || -z "${SLING_ADMIN_PASSWORD:-}" ]]; then
  echo "Set SLING_ADMIN_USER and SLING_ADMIN_PASSWORD (copy .env.example to .env)." >&2
  exit 1
fi

export SLING_ADMIN_USER SLING_ADMIN_PASSWORD

SLING_URL="${SLING_URL:-http://127.0.0.1:8080}"
SLING_URL="${SLING_URL%/}"
SLING_HOST="${SLING_URL#*://}"
SLING_HOST="${SLING_HOST%%/*}"
SLING_HOST="${SLING_HOST%%:*}"

mvn -B -f "${ROOT}/cheesecake-site/pom.xml" clean install sling:install -pl core
mvn -B -f "${ROOT}/cheesecake-site/pom.xml" -pl ui.content package

ZIP="${ROOT}/cheesecake-site/ui.content/target/cheesecake-site-content-1.0-SNAPSHOT.zip"
if [[ ! -f "${ZIP}" ]]; then
  echo "Content package not found at ${ZIP}" >&2
  exit 1
fi

NETRC="$(mktemp)"
chmod 600 "${NETRC}"
trap 'rm -f "${NETRC}"' EXIT
printf 'machine %s login %s password %s\n' "${SLING_HOST}" "${SLING_ADMIN_USER}" "${SLING_ADMIN_PASSWORD}" > "${NETRC}"

upload_json="$(curl --netrc-file "${NETRC}" --fail -sS \
  -F "file=@${ZIP};type=application/zip" \
  -F "force=true" \
  "${SLING_URL}/bin/cpm/package.upload.json")"
pkg_path="$(printf '%s' "${upload_json}" | grep -o '"path":"[^"]*"' | head -n 1 | sed 's/^"path":"//; s/"$//')"
if [[ -z "${pkg_path}" ]]; then
  echo "Composum upload did not return a package path." >&2
  exit 1
fi

install_json="$(curl --netrc-file "${NETRC}" --fail -sS \
  -F "path=${pkg_path}" \
  "${SLING_URL}/bin/cpm/package.install.json")"
if ! printf '%s' "${install_json}" | grep -q '"status":"done"'; then
  echo "Composum install did not finish: ${install_json}" >&2
  exit 1
fi

"${ROOT}/scripts/flush-dispatcher.sh"
