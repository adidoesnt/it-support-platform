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
