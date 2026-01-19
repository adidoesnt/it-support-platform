# IT Support Platform

## Overview

This project aims to be a production-standard IT incident orchestration service.
It accepts incidents via an API, processes them asynchronously using a step-based workflow.
It validates the requests, enriches the provided issue descriptions with AI-powered classification and produces human-facing support tickets.
The system provides full observability and provides idempotency guarantees for reported issues.

## Architecture

### Architecture Diagram

```mermaid
flowchart LR
    A[Client] --> B[API Service]
    B --> C[(Postgres)]
    B --> D[SQS Queue]
    D --> E[Worker Service]
    E --> C
    E --> F[LLM Interface]
    F -.-> G[Ollama]
    F -.-> H[OpenAI]
    F -.-> I[Other Providers]

    B --> P[Prometheus]
    E --> P
    P --> GRAF[Grafana]
```

### System Components

The system architecture comprises the following components:

1. **API Service**
   * Accepts an incident report via REST
   * Offloads the request to the SQS Queue
   * Acknowledges the request
2. **SQS Queue** (via LocalStack)
   * Receives messages from the API Service
3. **Worker Service**
   * Consumes messages from the SQS Queue (via polling)
   * Validates the message payload
   * Classifies the incident via the LLM Interface
   * Creates a human-facing ticket
4. **LLM Interface**
   * Interface implemented by specific LLM providers (Ollama, OpenAI etc.)
   * Classifies tickets into categories
5. **Prometheus**
   * Scrapes metrics from API and Worker services
   * Connects to Grafana as a data source
6. **Grafana**
   * Uses Prometheus as a data source
   * Provides visualisation and dashboards based on provided metrics

## Workflow

### Workflow Status State Machine

These are the states a job can be in and the transitions between them.

```mermaid
stateDiagram-v2
    [*] --> PENDING
    PENDING --> IN_PROGRESS
    IN_PROGRESS --> COMPLETED
    PENDING --> FAILED
    IN_PROGRESS --> FAILED
```

### Workflow Steps

These are the steps a job goes through while it is in the `IN_PROGRESS` state.

```mermaid
flowchart LR
    PAYLOAD_VALIDATION --> INCIDENT_CLASSIFICATION --> TICKET_CREATION
```

### Additional Notes

* Each submission triggers one workflow run.
* The submissions are idempotent, which is enforced using an `Idempotency-Key` header that is sent along with the request.
* If a request is submitted with an `Idempotency-Key` for which a workflow has already been triggered, the existing workflow ID will be returned.

## API Design

The API service exposes the following endpoints to the client:

### `POST /incidents` (API only)

#### Request Structure

**Headers**

* `Idempotency-Key`: unique key for request deduplication

**Body**

```json
{
  "description": "Users cannot access the VPN from the London office."
}
```

#### Response Structure

**Body**

```json
{
  "workflowRunId": 123
}
```

> **Note:** If a request is submitted with an `Idempotency-Key` for which a workflow has already been triggered, the existing workflow ID associated with the provided key will be returned.

### Actuator Endpoints (API + Worker)

The following actuator endpoints are exposed by both the API and Worker services:

* `/actuator/health`
* `/actuator/info`
* `/actuator/prometheus` (scraped by Prometheus to collect metrics)

## Idempotency & Reliability

The `Idempotency-Key` header has been mentioned a few time throughout this document.

It is a concept that was largely popularised by [Stripe](https://stripe.com/) in order to prevent duplicate side effects from create/submit operations.

In this system, if a request is submitted with an `Idempotency-Key` for which a workflow has already been triggered, the existing workflow ID associated with the provided key will be returned.

This prevents duplicate tickets being opened for the same issue upon retries due to timeouts or network errors on the client.

The `Idempotency-Key` combined with the SQS ensure **at-least-once** delivery.

## AI/LLM Integration

The system is designed to use AI as part of its infrastructure via the LLM Interface component.

The component was designed to be vendor-agnostic. This was achieved using the **strategy pattern**. The intention was for the `OllamaClient` to be used locally and for the `OpenAIClient` to be used in production.

```mermaid
classDiagram
    class LlmClient {
        <<interface>>
        +generateText(prompt)
    }

    class OllamaClient {
        +generateText(prompt)
    }

    class OpenAiClient {
        +generateText(prompt)
    }

    LlmClient <|.. OllamaClient
    LlmClient <|.. OpenAiClient
```

## Data Model

### Entity Relationship Diagram

```mermaid
erDiagram
    INCIDENTS ||--o{ WORKFLOW_RUNS : has
    WORKFLOW_RUNS ||--o| INCIDENT_CLASSIFICATIONS : produces
    WORKFLOW_RUNS ||--o| TICKETS : creates
    WORKFLOW_RUNS ||--o{ IDEMPOTENCY_KEYS : keyed_by
    INCIDENTS ||--o{ INCIDENT_CLASSIFICATIONS : classified_as
    INCIDENTS ||--o{ TICKETS : has

    INCIDENTS {
      bigint id PK
      text description
      timestamp created_at
      timestamp updated_at
    }

    WORKFLOW_RUNS {
      bigint id PK
      bigint incident_id FK
      varchar current_step
      varchar status
      timestamp created_at
      timestamp updated_at
    }

    IDEMPOTENCY_KEYS {
      varchar key PK
      bigint workflow_run_id FK
      timestamp created_at
      timestamp updated_at
    }

    INCIDENT_CLASSIFICATIONS {
      bigint id PK
      bigint workflow_run_id FK
      bigint incident_id FK
      varchar category
      varchar priority
      text summary
      varchar model_provider
      varchar model_name
      text raw_response
      timestamp created_at
      timestamp updated_at
    }

    TICKETS {
      bigint id PK
      bigint incident_id FK
      bigint workflow_run_id FK
      text title
      text description
      text status
      timestamp created_at
      timestamp updated_at
    }
```

### Table Descriptions

* `incidents`: Incoming incident reports submitted by clients.
* `workflow_runs`: Tracks the step and status of processing for each incident.
* `idempotency_keys`: Deduplication keys mapped to workflow runs for safe retries.
* `incident_classifications`: LLM-generated classification results tied to a workflow run.
* `tickets`: Final human-facing tickets created from classified incidents.

### Why tie tickets to workflow runs?

Tickets are tied because the ticket generation is the output of a workflow run rather than an incident report. It also ensures **at-least-once processing** and **exactly-once ticket creation** The incident ID is is also stored in the tickets table for convenience.

## Observability and Debuggability

The system includes observability to support production debugging and performance analysis.

### Components

* **Spring Boot Actuator** exposes service health and metrics endpoints.
* **Micrometer** measures built-in and custom metrics for both API and Worker services.
* **Prometheus** scrapes `/actuator/prometheus` on each service to collect metrics.
* **Grafana** is used to visualise metrics via dashboards, providing operational insight

### Custom metrics tracked

* **Incidents received** (API): total incident submissions.
  * Allows verification of request reception when clients report issues
  * Allows us to correlate load with latency/failures
* **Workflow step success/failure** (Worker): Tagged by step for completion and error rates.
  * Allows us to find bottlenecks by identifying steps with high failure rates
* **Step latency (P95)** (Worker): The slowest 5% of instances for each workflow step
  * Allows to pinpoint high-latency steps

## Local Development

### Prerequisites

* Java 21
* Docker + Docker Compose
* PostgreSQL client (optional, for local inspection)
* LocalStack (via Docker)
* Ollama (for local LLM classification)

### Startup

1. Start infrastructure services:
   ```bash
   cd infra
   docker compose up -d
   ```
2. Run the API service:
   ```bash
   ./gradlew :services:api:bootRun
   ```
3. Run the worker service:
   ```bash
   ./gradlew :services:worker:bootRun
   ```
4. Verify:
   * API: `http://localhost:8080/actuator/health`
   * Worker: `http://localhost:8081/actuator/health`
   * Prometheus: `http://localhost:9090`
   * Grafana: `http://localhost:3000`

### Usage

#### Create an Incident

1. Submit a new incident:
   ```bash
   curl -X POST http://localhost:8080/incidents \
     -H "Content-Type: application/json" \
     -H "Idempotency-Key: demo-incident-001" \
     -d '{"description":"Users cannot access the VPN from the London office."}'
   ```
2. Example response:
   ```json
   { "workflowRunId": 123 }
   ```
3. The API acknowledges the request and enqueues the workflow run.
4. The worker consumes the queue message, processes the workflow steps, and creates a ticket.
5. Verify in the database:
   ```sql
   SELECT * FROM workflow_runs WHERE id = 123;
   SELECT * FROM tickets WHERE workflow_run_id = 123;
   -- Substitute "123" with the workflowRunId returned in step 2
   ```
