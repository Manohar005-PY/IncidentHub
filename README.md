# IncidentHub API

IncidentHub API is a Spring Boot REST service for creating and tracking incidents. It currently supports incident creation, lookup, status transitions, and paginated listing with severity and status filters.

## Features

- Create incidents with a validated title and severity.
- Retrieve an incident by ID.
- List incidents using optional severity and status filters.
- Paginate and sort incident results.
- Transition incidents through an explicit lifecycle.
- Return consistent JSON error messages for application-level errors.
- Persist data in PostgreSQL, with schema changes managed by Flyway.
- Use JPA optimistic locking to detect conflicting updates.

## Technology

- Java 21
- Spring Boot 4.1.1
- Spring MVC
- Spring Data JPA
- Jakarta Bean Validation
- PostgreSQL
- Flyway
- Maven
- JUnit and Spring MVC test support

## Requirements

- JDK 21
- Maven
- PostgreSQL

## Configuration and running locally

The application is configured through `src/main/resources/application.properties`. The default datasource URL points to a local PostgreSQL database named `incidenthub`, with the username `incidenthub`. Configure the datasource credentials for your environment before starting the application. Do not commit real passwords or other secrets.

Create the database and user in PostgreSQL, then set `spring.datasource.url`, `spring.datasource.username`, and `spring.datasource.password` to matching values. For example, a local URL can look like:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/incidenthub
spring.datasource.username=incidenthub
spring.datasource.password=<your-local-password>
```

Run the application from the project root:

```bash
mvn spring-boot:run
```

By default, the API will be available at `http://localhost:8080`.

Flyway applies migrations from `src/main/resources/db/migration`. The initial migration creates the `incidents` table, and the next migration adds the optimistic-locking `version` column. Hibernate is configured to validate the schema rather than create or update it automatically.

## API

All request and response bodies use JSON unless otherwise noted.

### Health check

`GET /api/health`

Returns a simple application health response:

```json
{
  "status": "UP",
  "application": "IncidentHub API"
}
```

### Create an incident

`POST /api/incidents`

Request:

```json
{
  "title": "Database connection failed",
  "severity": "HIGH"
}
```

Validation:

- `title` must not be blank and may contain at most 255 characters.
- `severity` must be one of `LOW`, `MEDIUM`, `HIGH`, or `CRITICAL`.

On success, returns `201 Created`, the created incident as JSON, and a `Location` header pointing to `/api/incidents/{id}`.

### Get an incident

`GET /api/incident/{id}`

Returns `200 OK` with the incident if it exists. Returns `404 Not Found` if there is no incident with that ID.

### Update incident status

`PATCH /api/incidents/{id}/status`

Request:

```json
{
  "status": "ACKNOWLEDGED"
}
```

Supported statuses and allowed transitions:

| Current status | Allowed next status |
|---|---|
| `OPEN` | `ACKNOWLEDGED` |
| `ACKNOWLEDGED` | `INVESTIGATING` |
| `INVESTIGATING` | `RESOLVED` |
| `RESOLVED` | `OPEN` |

Returns `200 OK` with the updated incident. A missing incident returns `404 Not Found`; a disallowed transition returns `400 Bad Request`.

### List incidents

`GET /api/incidents`

Supported query parameters:

| Parameter | Default | Description |
|---|---:|---|
| `severity` | — | Optional severity filter: `LOW`, `MEDIUM`, `HIGH`, or `CRITICAL`. Case-insensitive. |
| `status` | — | Optional status filter: `OPEN`, `ACKNOWLEDGED`, `INVESTIGATING`, or `RESOLVED`. Case-insensitive. |
| `page` | `0` | Zero-based page number. Must not be negative. |
| `size` | `10` | Page size from 1 through 100. |
| `sortBy` | `id` | Sort field: `id`, `title`, `severity`, `status`, or `version`. Results are sorted ascending. |

Example:

```bash
curl 'http://localhost:8080/api/incidents?severity=high&status=open&page=0&size=20&sortBy=title'
```

Successful listing returns `200 OK` with the following page shape:

```json
{
  "content": [
    {
      "id": 1,
      "title": "Database connection failed",
      "severity": "HIGH",
      "status": "OPEN"
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 1,
  "totalPages": 1,
  "last": true
}
```

Invalid page numbers, page sizes, sort fields, severities, or statuses return `400 Bad Request`. The controller currently uses ascending sort order; there is no query parameter for selecting the direction.

## Error responses

Application-level errors handled by the global exception handler return a JSON body with a `message` field. For example:

```json
{
  "message": "Invalid status transition from OPEN to RESOLVED"
}
```

The HTTP response status indicates the error category, including `400 Bad Request`, `404 Not Found`, and `409 Conflict` for optimistic-locking conflicts. Framework-level request validation errors, such as a missing or invalid body field, are also rejected with `400 Bad Request`.

## Persistence and concurrency

The `Incident` entity is stored in the `incidents` table. Flyway migrations are located in `src/main/resources/db/migration`:

- `V1__create_incidents_table.sql` creates the initial table.
- `V2__add_version_to_incidents.sql` adds the version column used by JPA optimistic locking.

The service layer uses transactions for writes and read-only transactions for lookups and searches. Concurrent updates that cause an optimistic-locking conflict are mapped to `409 Conflict`.

## Tests

Run the full test suite with:

```bash
mvn test
```

The tests cover service behavior and controller request/response behavior, including:

- Request validation when creating incidents and updating status.
- Successful and invalid status transitions.
- Missing incidents.
- Incident listing with severity and status filters.
- Invalid listing parameters such as a negative page, excessive page size, unsupported sort field, and invalid filters.

The application context test starts the full application and therefore requires a reachable PostgreSQL instance with the configured credentials. Controller and service tests use test doubles and do not require a live database.

## Project layout

```text
src/
  main/
    java/com/manohar/incidenthub/
      controller and application entry point
      dto/           request and response models
      exception/     domain exceptions and API exception handling
      model/         Incident entity and status enum
      service/       transactional incident operations
      repositroy/    Spring Data JPA repository
    resources/
      db/migration/  Flyway SQL migrations
  test/
    java/            controller, service, and application tests
```
