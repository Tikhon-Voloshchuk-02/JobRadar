# JobRadar Backup and Restore

Infrastructure task: `INFRA-001`

## Objectives

- Recovery Point Objective (RPO): 24 hours or less
- Preliminary Recovery Time Objective (RTO): 2 hours or less
- Back up both PostgreSQL data and uploaded documents
- Keep completed and incomplete backups clearly separated
- Verify every backup with SHA-256 checksums
- Regularly prove recoverability through an isolated restore test

## Backup contents

Each completed backup is stored in a directory named:

```text
jobradar-YYYY-MM-DDTHHMMSSZ/
