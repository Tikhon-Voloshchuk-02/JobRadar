#!/usr/bin/env bash

set -Eeuo pipefail
umask 077
SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
readonly SCRIPT_DIR

PROJECT_DIR="$(cd -- "$SCRIPT_DIR/../.." && pwd)"
readonly PROJECT_DIR

readonly BACKUP_ROOT="${BACKUP_ROOT:-/var/backups/jobradar}"

CREATED_AT_UTC="$(date -u +%Y-%m-%dT%H%M%SZ)"
readonly CREATED_AT_UTC

readonly BACKUP_NAME="jobradar-${CREATED_AT_UTC}"
readonly TEMP_DIR="${BACKUP_ROOT}/.${BACKUP_NAME}.incomplete"
readonly FINAL_DIR="${BACKUP_ROOT}/${BACKUP_NAME}"

cleanup() {
    local exit_code=$?

    trap - EXIT INT TERM

    if (( exit_code != 0 )); then
        printf 'Backup failed with exit code %d\n' "$exit_code" >&2

        if [[ -d "$TEMP_DIR" ]]; then
            rm -rf -- "$TEMP_DIR"
        fi
    fi

    exit "$exit_code"
}

require_command() {
    local command_name=$1

    if ! command -v "$command_name" >/dev/null 2>&1; then
        printf 'Required command not found: %s\n' "$command_name" >&2
        exit 1
    fi
}

trap cleanup EXIT INT TERM

require_command docker
require_command tar
require_command sha256sum
require_command flock

if [[ ! -d "$PROJECT_DIR/uploads" ]]; then
    printf 'Uploads directory not found: %s\n' "$PROJECT_DIR/uploads" >&2
    exit 1
fi

mkdir -p -- "$BACKUP_ROOT"

exec 9>"$BACKUP_ROOT/.backup.lock"

if ! flock -n 9; then
    printf 'Another JobRadar backup is already running\n' >&2
    exit 1
fi

if [[ -e "$TEMP_DIR" || -e "$FINAL_DIR" ]]; then
    printf 'Backup path already exists for timestamp %s\n' "$CREATED_AT_UTC" >&2
    exit 1
fi

mkdir -- "$TEMP_DIR"

printf 'Creating PostgreSQL dump...\n'

docker compose \
    --project-directory "$PROJECT_DIR" \
    -f "$PROJECT_DIR/docker-compose.yml" \
    exec -T db \
    pg_dump -U postgres -d jobradar -Fc \
    > "$TEMP_DIR/database.dump"

if [[ ! -s "$TEMP_DIR/database.dump" ]]; then
    printf 'PostgreSQL dump is empty\n' >&2
    exit 1
fi

printf 'Archiving uploads...\n'

tar \
    -C "$PROJECT_DIR" \
    -czf "$TEMP_DIR/uploads.tar.gz" \
    uploads

{
    printf 'backup_format_version=1\n'
    printf 'created_at_utc=%s\n' "$CREATED_AT_UTC"
    printf 'hostname=%s\n' "$(hostname)"
    printf 'database=jobradar\n'
    printf 'database_format=postgresql_custom\n'
    printf 'uploads_file_count=%s\n' \
        "$(find "$PROJECT_DIR/uploads" -type f | wc -l)"
    printf 'pg_dump_version=%s\n' \
        "$(docker compose \
            --project-directory "$PROJECT_DIR" \
            -f "$PROJECT_DIR/docker-compose.yml" \
            exec -T db pg_dump --version)"
} > "$TEMP_DIR/manifest.txt"

(
    cd "$TEMP_DIR"
    sha256sum database.dump uploads.tar.gz manifest.txt > SHA256SUMS
    sha256sum --check SHA256SUMS
)

mv -- "$TEMP_DIR" "$FINAL_DIR"

trap - EXIT INT TERM

printf 'Backup completed: %s\n' "$FINAL_DIR"
