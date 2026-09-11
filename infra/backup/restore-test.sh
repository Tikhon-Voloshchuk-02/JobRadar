#!/usr/bin/env bash

set -Eeuo pipefail
umask 077

if (( $# != 1 )); then
    printf 'Usage: %s BACKUP_DIRECTORY\n' "$0" >&2
    exit 2
fi

BACKUP_DIR="$(realpath -- "$1")"
readonly BACKUP_DIR

readonly CONTAINER_NAME="jobradar-restore-test-$$"

RESTORE_DIR="$(mktemp -d "${TMPDIR:-/tmp}/jobradar-restore.XXXXXX")"
readonly RESTORE_DIR

readonly START_SECONDS=$SECONDS

cleanup() {
    local exit_code=$?

    trap - EXIT INT TERM

    docker rm -f "$CONTAINER_NAME" >/dev/null 2>&1 || true
    rm -rf -- "$RESTORE_DIR"

    exit "$exit_code"
}

trap cleanup EXIT INT TERM

for required_file in \
    database.dump \
    uploads.tar.gz \
    manifest.txt \
    SHA256SUMS
do
    if [[ ! -f "$BACKUP_DIR/$required_file" ]]; then
        printf 'Required backup file not found: %s\n' "$required_file" >&2
        exit 1
    fi
done

printf 'Verifying checksums...\n'

(
    cd "$BACKUP_DIR"
    sha256sum --check SHA256SUMS
)

printf 'Starting isolated PostgreSQL...\n'

docker run -d \
    --name "$CONTAINER_NAME" \
    --network none \
    -e POSTGRES_PASSWORD=restore-test-only \
    -e POSTGRES_DB=jobradar_restore \
    postgres:15 \
    >/dev/null

initialization_complete=false

for _ in {1..60}; do
    if docker logs "$CONTAINER_NAME" 2>&1 \
        | grep -q 'PostgreSQL init process complete'
    then
        initialization_complete=true
        break
    fi

    sleep 1
done

if [[ "$initialization_complete" != true ]]; then
    printf 'Temporary PostgreSQL initialization did not complete\n' >&2
    docker logs "$CONTAINER_NAME" >&2
    exit 1
fi

database_ready=false

for _ in {1..30}; do
    if docker exec "$CONTAINER_NAME" \
        pg_isready -U postgres -d jobradar_restore \
        >/dev/null 2>&1
    then
        database_ready=true
        break
    fi

    sleep 1
done

if [[ "$database_ready" != true ]]; then
    printf 'Temporary PostgreSQL did not become ready after initialization\n' >&2
    docker logs "$CONTAINER_NAME" >&2
    exit 1
fi

printf 'Restoring PostgreSQL dump...\n'

docker exec -i "$CONTAINER_NAME" \
    pg_restore \
    -U postgres \
    -d jobradar_restore \
    --exit-on-error \
    --no-owner \
    --no-privileges \
    < "$BACKUP_DIR/database.dump"

restored_tables="$(
    docker exec "$CONTAINER_NAME" \
        psql -U postgres -d jobradar_restore -Atc \
        "SELECT COUNT(*)
         FROM information_schema.tables
         WHERE table_schema = 'public'
           AND table_type = 'BASE TABLE';"
)"

if (( restored_tables == 0 )); then
    printf 'Restore produced no application tables\n' >&2
    exit 1
fi

printf 'Restoring uploads...\n'

tar -xzf "$BACKUP_DIR/uploads.tar.gz" -C "$RESTORE_DIR"

expected_uploads="$(
    awk -F= \
        '$1 == "uploads_file_count" { print $2 }' \
        "$BACKUP_DIR/manifest.txt"
)"

actual_uploads="$(
    find "$RESTORE_DIR/uploads" -type f | wc -l
)"

if [[ "$expected_uploads" != "$actual_uploads" ]]; then
    printf 'Uploads mismatch: expected=%s actual=%s\n' \
        "$expected_uploads" "$actual_uploads" >&2
    exit 1
fi

printf 'Restore test successful\n'
printf 'Restored tables: %s\n' "$restored_tables"
printf 'Restored uploads: %s\n' "$actual_uploads"
printf 'Duration: %s seconds\n' "$((SECONDS - START_SECONDS))"
