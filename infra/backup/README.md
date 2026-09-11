# JobRadar Backup and Restore

Infrastructure task: `INFRA-001`

## Objectives

- Recovery Point Objective (RPO): 24 hours or less
- Preliminary Recovery Time Objective (RTO): 2 hours or less
- Back up PostgreSQL data and uploaded documents
- Verify every backup with SHA-256 checksums
- Regularly prove recoverability through an isolated restore test

## Backup contents

Each completed backup is stored in:

```text
jobradar-YYYY-MM-DDTHHMMSSZ/
├── database.dump
├── uploads.tar.gz
├── manifest.txt
└── SHA256SUMS
```

`database.dump` uses the PostgreSQL custom format created by `pg_dump -Fc`.

## Create a backup

For local testing:

```bash
BACKUP_ROOT="$HOME/jobradar-backups" ./infra/backup/backup.sh
```

The planned production location is:

```text
/var/backups/jobradar
```

## Run an isolated restore test

```bash
./infra/backup/restore-test.sh /path/to/jobradar-backup
```

The restore test:

1. verifies the checksums;
2. starts an isolated PostgreSQL 15 container;
3. restores the database;
4. checks that application tables exist;
5. restores uploaded documents;
6. compares the expected and actual upload counts;
7. removes the temporary environment.

The production database and its Docker volume are not modified.

## Safety properties

- Scripts stop on errors and unset variables.
- Files are created with owner-only permissions.
- `flock` prevents concurrent backup executions.
- Data is first written into an `.incomplete` directory.
- A backup receives its final name only after successful verification.
- Failed temporary backups are removed automatically.

## Retention

Backup v1 will retain:

- 7 daily backups
- 4 weekly backups

## Recovery scope

Backup v1 stores backups on the same server as the application.

It protects against:

- accidental database or Docker volume deletion;
- unsuccessful migrations;
- logical data corruption;
- accidental deletion of uploaded files.

It does not protect against:

- complete VPS loss;
- physical disk failure;
- loss of access to the hosting provider;
- an attacker deleting production data and local backups.

Complete server loss is an accepted risk for Backup v1.
Off-site storage may be added later if the value or usage of JobRadar increases.

The preliminary RTO objective applies only while the original server remains available.

## Out of scope for v1

- Off-site storage
- Point-in-time recovery
- WAL archiving
- Automated database failover
