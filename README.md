# Kedada API

Initial Kedada backend, a REST API for registering, querying, searching, and organizing events in El Salvador.

## Architecture

The project uses Spring Boot 4, Java 25, Maven, PostgreSQL, Spring Web MVC, Spring Data JPA, Bean Validation, Flyway, and springdoc-openapi.

The structure follows domain-based layers:

- `controller`: REST endpoints and input validation.
- `service`: business rules, transactions, and reference validation.
- `repository`: JPA data access and native queries.
- `entity`: JPA persistence model.
- `dto`: public request/response contracts.
- `mapper`: entity/DTO conversion.
- `common`: shared errors, responses, and validations.

## Technical Decisions

- `Event.thumbnail` is kept as a plain `UUID` because there is no files table yet.
- `owner_id` is modeled as a soft `UUID` reference, without a foreign key, to allow JWT/Auth integration later.
- `Schedule` did not have `event_id` in the original model. A nullable `event_id uuid references events(id)` was added to support global or not-yet-associated schedules without blocking the model's evolution.
- `EventMetricDaily` uses a composite key `(event_id, day)` for daily metrics per event.
- View and share increments use `insert ... on conflict do update`, which is safe under concurrent access in PostgreSQL.
- Events, categories, and URLs use soft delete with `is_deleted` and `deleted_at`; normal listings do not return deleted records.
- A category or URL cannot be soft-deleted while an active event references it.
- Text search uses `search_vector` with a trigger and GIN index.

## Main Tree

```text
src/main/java/com/kedada/backend
  KedadaBackendApplication.java
  config/OpenApiConfig.java
  common/
    exception/
    response/
    validation/
  event/
    controller/
    service/
    repository/
    entity/
    dto/
    mapper/
  category/
    controller/
    service/
    repository/
    entity/
    dto/
    mapper/
  url/
    controller/
    service/
    repository/
    entity/
    dto/
    mapper/
  schedule/
    controller/
    service/
    repository/
    entity/
    dto/
    mapper/
  metric/
    controller/
    service/
    repository/
    entity/
    dto/
src/main/resources
  application.yml
  db/migration/V1__create_initial_schema.sql
```

## Run Locally

```bash
docker compose up -d
mvn spring-boot:run
```

Swagger UI:

```text
http://localhost:8080/swagger-ui.html
```

## JSON Examples

Create a category:

```json
{
  "name": "Music",
  "ownerId": "11111111-1111-1111-1111-111111111111",
  "type": ["concert", "festival"]
}
```

Create a URL:

```json
{
  "url": "https://example.com/event",
  "description": "Official site",
  "ownerId": "11111111-1111-1111-1111-111111111111",
  "kind": "official"
}
```

Create an event:

```json
{
  "title": "Festival in San Salvador",
  "description": "Cultural event open to the public.",
  "priority": 1,
  "thumbnail": null,
  "price": 12.50,
  "siteUrlId": "22222222-2222-2222-2222-222222222222",
  "referenceUrlId": null,
  "categoryId": "33333333-3333-3333-3333-333333333333"
}
```

Create a schedule:

```json
{
  "eventId": "44444444-4444-4444-4444-444444444444",
  "startDate": "2026-06-01T19:00:00-06:00",
  "endDate": "2026-06-01T22:00:00-06:00",
  "ownerId": "11111111-1111-1111-1111-111111111111"
}
```

List only the schedules for an event:

```bash
curl "http://localhost:8080/api/v1/schedules?eventId=44444444-4444-4444-4444-444444444444&sort=startDate,asc"
```

Register a view or share:

```bash
curl -X POST "http://localhost:8080/api/v1/events/{eventId}/view?ownerId=11111111-1111-1111-1111-111111111111"
curl -X POST "http://localhost:8080/api/v1/events/{eventId}/share?ownerId=11111111-1111-1111-1111-111111111111"
```

Search events:

```text
GET /api/v1/events?q=festival&categoryId={uuid}&minPrice=0&maxPrice=25&priority=1&fromDate=2026-06-01T00:00:00-06:00&page=0&size=20&sort=createdAt,desc
```

Build and run with Docker:

```bash
docker build -t kedada-api:local .
docker compose up -d postgres
docker run --rm -p 8080:8080 \
  --network kedada-api_default \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/kedada \
  -e SPRING_DATASOURCE_USERNAME=kedada \
  -e SPRING_DATASOURCE_PASSWORD=kedada \
  kedada-api:local
```

## Recommended Follow-Ups

- Add authentication and resolve `owner_id` from JWT instead of request/query parameters.
- Add integration tests with Testcontainers for PostgreSQL and Flyway.
- Tune search ranking (`ts_rank`) once there is a results UX.
