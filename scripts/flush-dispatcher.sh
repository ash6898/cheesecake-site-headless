#!/usr/bin/env bash
# POST the dispatcher flush endpoint. Use this after content changes.
# AEM would do this from a flush replication agent; see the README.
set -euo pipefail

DISPATCHER_URL="${DISPATCHER_URL:-http://127.0.0.1:8081}"
curl --fail-with-body -sS -X POST "${DISPATCHER_URL%/}/dispatcher/invalidate"
echo
