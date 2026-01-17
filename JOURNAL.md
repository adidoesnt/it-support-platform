# Journal

This document keeps track of activities carried out during the development of this project.

# Entries

## 17-01-2026

- Installed Open JDK 21
- Created Gradle multi-project monorepo containing:
  1. Root project
     - Gradle wrapper, shared config
     - No application code
  2. API (`services/api`): HTTP-facing service
  3. Worker (`services/worker`): Background processing/async orchestration
- Configured supporting infrastructure via `docker-compose`:
  1. Postgres: For persistence
  2. LocalStack: AWS services emulation
  3. Prometheus: Collection of metrics
  4. Grafana: Visualisation of collected metrics
- Installed initial dependencies for API:
  ```
  implementation 'org.springframework.boot:spring-boot-starter-web'
  implementation 'org.springframework.boot:spring-boot-starter-actuator'
  implementation 'io.micrometer:micrometer-registry-prometheus'
  ```
- Ensured API boots cleanly using:
  ```bash
  ./gradlew :services:api:bootRun  # --info --stacktrace (optional)
  ```
- Created database schema (under `services/api/src/main/resources/db/migration`)
- Added dependencies for `flyway`, our migration tool:

  ```
  implementation 'org.springframework.boot:spring-boot-starter-jdbc'
  implementation 'org.springframework.boot:spring-boot-starter-flyway'
  implementation 'org.flywaydb:flyway-database-postgresql'

  runtimeOnly 'org.postgresql:postgresql'
  ```

- Updated application config at `services/api/src/main/resources/application.yaml`:
  ```yaml
  spring:
  [... other config]
  datasource:
    url: jdbc:postgresql://localhost:5434/it_support_platform
    username: postgres
    password: postgres
  ```
- Ran app to auto-run migration
- Added a placeholder `POST /incidents` endpoint:
  1. Created Incident and WorkflowRun classes (and workflow step and workflow status enums)
  2. Created Incident Controller and Create Incident Response classes
- Added logic for the `POST /incidents` endpoint:
  1. Check for `Idempotency-Key` header, return 400 if not present
  2. Check for existing Idempotency Key entity:
    - If present, return associated workflow run
    - Else, create a new incident, workflow run and associate it with a new Idempotency Key entity
