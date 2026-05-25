# AGENTS.md

This file is a quick orientation guide for coding agents working in this repo.
Use it together with `README.md`; the README explains the product and examples,
while this file focuses on how to navigate and change the code safely.

## Repository Snapshot

Kedada API is a Spring Boot REST backend for registering, searching, organizing,
and measuring events in El Salvador.

- Language/runtime: Java 25.
- Framework: Spring Boot 4.0.0 with Spring Web MVC.
- Build tool: Maven.
- Database: PostgreSQL 17 locally, Flyway-managed schema.
- Persistence: Spring Data JPA plus native SQL for event search and metrics.
- Auth: Spring Security with stateless Bearer JWT.
- API docs: springdoc-openapi at `/swagger-ui.html`.
- Main package: `com.kedada.backend`.

## How To Run

Start PostgreSQL:

```bash
docker compose up -d
```

Run the app:

```bash
mvn spring-boot:run
```

Useful local URLs:

- App: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

Verification commands:

```bash
mvn test
mvn package
```

There are currently no dedicated test source files in the repo, so `mvn test`
mostly verifies compilation and Spring/Maven wiring unless tests are added.

## Configuration

Primary config lives in `src/main/resources/application.yml`.

- Active profile defaults to `local`.
- Local datasource: `jdbc:postgresql://localhost:5432/kedada`.
- Local credentials: `kedada` / `kedada`.
- `spring.jpa.hibernate.ddl-auto=validate`; schema changes should be made with
  Flyway migrations, not Hibernate auto-DDL.
- `spring.jpa.open-in-view=false`; keep service methods transactional where lazy
  relationships or persistence state matter.
- JWT settings live under `kedada.security.jwt`; local/test have development
  secrets, while `prod` expects `JWT_SECRET`.
- `prod` expects `DATABASE_URL`, `DATABASE_USERNAME`, `DATABASE_PASSWORD`, and
  `JWT_SECRET`.

## Project Structure

The code is organized by domain, and each domain follows the same layered shape:

```text
src/main/java/com/kedada/backend
  KedadaBackendApplication.java
  config/
  common/
    exception/
    response/
    validation/
  auth/
    controller/
    service/
    repository/
    entity/
    dto/
    security/
  category/
    controller/
    service/
    repository/
    entity/
    dto/
    mapper/
  event/
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
  schedule/
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
  media/
    controller/
    service/
    repository/
    entity/
    dto/
```

Layer responsibilities:

- `controller`: REST endpoints, request validation annotations, HTTP status.
- `service`: business rules, reference checks, transactions.
- `repository`: JPA repositories and native SQL query implementations.
- `entity`: JPA persistence model.
- `dto`: public request/response contracts.
- `mapper`: entity/DTO conversion.
- `common`: shared error responses, exceptions, and validation helpers.

## Domain Map

### Auth

Endpoint base: `/api/v1/auth`.

Users are stored in the `users` table with `email`, `password_hash`, `name`,
and `role`. Passwords are encoded with BCrypt. Auth is stateless: register or
login returns a Bearer JWT, and protected endpoints read the authenticated user
from `@AuthenticationPrincipal AuthenticatedUser`.

- `POST /api/v1/auth/register` creates a user and returns a token.
- `POST /api/v1/auth/login` returns a token for valid credentials.
- `GET /api/v1/**`, Swagger/OpenAPI, auth endpoints, and event view/share
  tracking remain public.
- Other mutating endpoints require `Authorization: Bearer <token>`.

### Categories

Endpoint base: `/api/v1/categories`.

Categories have `id`, `name`, `ownerId`, and `type` as a list/string array.
Creates assign `ownerId` from the authenticated user. Updates and deletes require
resource ownership or an authenticated `ADMIN` role. Deletion is soft delete, but `CategoryService.delete` blocks
deletion when any active event references the category and returns a conflict
through `BusinessConflictException`.

### URLs

Endpoint base: `/api/v1/urls`.

URLs have `id`, nullable `eventId`, `url`, `description`, `ownerId`, and
`kind`. An event may have multiple URL rows. Deletion is hard delete in the API
contract but implemented as soft delete. Creates assign `ownerId` from the
authenticated user; an optional `eventId` must refer to an active event owned
by that user. Updates and deletes require ownership.

`GET /api/v1/urls?eventId={eventId}` filters URL listings for one active event
while retaining the normal pagination and sorting parameters.

### Events

Endpoint base: `/api/v1/events`.

Events have title, description, priority, optional thumbnail UUID, optional
price, `visibleOnWebsite`, one or more required categories, timestamps,
soft-delete fields, `ownerId`, and a PostgreSQL `tsvector` search column.

Important event behavior:

- Creates default `priority` to `1` when omitted.
- Creates assign `ownerId` from the authenticated user.
- Creates default `visibleOnWebsite` to `true` when omitted.
- Create/update require at least one category and only allow categories owned by
  the same authenticated user.
- Anonymous reads only return events where `visibleOnWebsite` is true;
  authenticated admin-panel users can read hidden active events and change
  website visibility regardless of owner.
- Authenticated panel users can update and soft-delete events regardless of
  owner.
- Deletes are soft deletes via `is_deleted` and `deleted_at`.
- Search is implemented by `EventSearchRepositoryImpl` with native SQL.
- Supported event sort fields are `createdAt`, `updatedAt`, `title`,
  `priority`, and `price`. Unsupported sort fields become `400 Bad Request`.
- Text search checks both Spanish and simple PostgreSQL dictionaries.
- Date filters use `exists` against `schedules.start_date`.
- `thumbnail`, when supplied, must reference a media image owned by the same user.

### Media

Endpoint base: `/api/v1/media`.

Images are uploaded through authenticated `POST /api/v1/media` multipart
requests and stored in a private S3-compatible bucket. The API records object
metadata in `media_assets`; event `thumbnail` values reference these stable
media UUIDs. `GET /api/v1/media/{id}` returns a short-lived signed read URL for
images attached to active events, or for an authenticated owner previewing an
uploaded image before association. Upload validation permits JPEG, PNG, WEBP,
and GIF images up to 5 MB and checks their file signatures.

### Schedules

Endpoint base: `/api/v1/schedules`.

Schedules have `id`, nullable `eventId`, `startDate`, nullable `endDate`, and
`ownerId`. A schedule may be global/unassociated because `event_id` is nullable.
Creates assign `ownerId` from the authenticated user. Updates and deletes require
ownership. When an `eventId` is provided, `ScheduleService` verifies the event is
active and owned by the authenticated user.

`GET /api/v1/schedules?eventId={eventId}` filters schedule listings for one
active event while retaining the normal pagination and sorting parameters.

Date ranges are validated in two places:

- Bean validation with `@ValidDateRange`.
- Database check constraint: `end_date is null or end_date > start_date`.

### Metrics

Endpoint base: `/api/v1/events/{eventId}/metrics`.

Metrics are daily per event using composite key `(event_id, day)`.

- `POST /api/v1/events/{id}/view` increments views for the event's stored owner.
- `POST /api/v1/events/{id}/share` increments shares for the event's stored owner.
- `GET /api/v1/events/{eventId}/metrics/daily?from=YYYY-MM-DD&to=YYYY-MM-DD` returns authenticated daily analytics over at most 366 days.
- `GET /api/v1/events/{eventId}/metrics/summary` returns total views/shares.

Metric increments use PostgreSQL `insert ... on conflict do update`, which is
safe under concurrent requests. Public tracking requests are bounded in memory
and deduplicated per event/client for repeated refreshes; production deployment
should additionally rate limit `/api/` at the edge for volumetric traffic.

## Database And Migrations

The initial schema is in:

```text
src/main/resources/db/migration/V1__create_initial_schema.sql
```

Key schema details:

- `pgcrypto` is enabled for `gen_random_uuid()`.
- `users` stores authenticated users.
- `events.owner_id`, `categories.owner_id`, `urls.owner_id`, and
  `schedules.owner_id` track resource ownership.
- `event_categories` associates an event with one or more categories and has
  primary key `(event_id, category_id)`.
- `urls.event_id` is nullable and references `events(id)`, allowing multiple
  links for each event.
- `schedules.event_id` is nullable and references `events(id)`.
- `media_assets` stores private S3 object metadata and `events.thumbnail`
  references `media_assets(id)`.
- `event_metric_daily` uses primary key `(event_id, day)`.
- `events.search_vector` is maintained by a trigger.
- `events.updated_at` is maintained by a trigger.

When changing persistence:

- Add a new Flyway migration instead of editing applied migrations once the
  schema may have been used by others.
- Keep JPA column names aligned with SQL names.
- Remember `ddl-auto=validate`; mismatched entities and schema will fail startup.

## Error Handling

Global error handling lives in `common/exception/GlobalExceptionHandler.java`.

Standard behavior:

- Bean validation errors: `400` with field-level details.
- Constraint violations, bad request bodies, type mismatches, illegal arguments,
  and invalid pageable sort properties: `400`.
- `ResourceNotFoundException`: `404`.
- `BusinessConflictException`: `409`.
- Authentication failures: `401`.
- Ownership/authorization failures: `403`.
- Unexpected exceptions: `500` with generic message.

Error bodies use `ErrorResponse` and field errors use `FieldErrorResponse`.

## Validation Patterns

DTOs use Jakarta Bean Validation annotations.

Examples:

- `EventCreateRequest.title`: required, max 100.
- `EventCreateRequest.categoryIds`: required, non-empty.
- `EventCreateRequest.priority`: minimum 1.
- `EventCreateRequest.price`: minimum 0.00.
- `UrlCreateRequest.url`: required and valid URL.
- `ScheduleCreateRequest`: class-level `@ValidDateRange`.
- Auth request DTOs validate email, password length, and user name.

For new endpoints, validate at DTO/controller boundaries and keep business
reference validation in services.

## Coding Conventions In This Repo

- Prefer constructor injection.
- Keep controllers thin and services transactional.
- Use records for request/response DTOs.
- Use mapper classes for entity/DTO conversion instead of conversion logic in
  controllers.
- Put domain-specific changes under the matching domain package.
- Use `ResourceNotFoundException` for missing records.
- Use `BusinessConflictException` for valid requests blocked by current state.
- Use `AccessDeniedException` for authenticated users trying to mutate resources
  they do not own.
- Preserve soft delete semantics for events.
- Do not trust client-provided `ownerId` for protected writes; derive ownership
  from `AuthenticatedUser`.

## Common Change Paths

Adding a simple field:

1. Add a Flyway migration for the database column.
2. Update the JPA entity.
3. Update request/response DTOs if the field is public.
4. Update mapper logic.
5. Update service create/update logic.
6. Add or adjust validation annotations.
7. Run `mvn test`.

Adding an endpoint:

1. Add the controller method under the relevant domain.
2. Keep transaction and business logic in the service.
3. Add repository methods only when persistence access is needed.
4. Return DTOs, not entities.
5. Make sure errors flow through the existing exception handler.

Changing protected writes:

1. Keep `GET` endpoints public unless the product requirement changes.
2. Add `@AuthenticationPrincipal AuthenticatedUser user` to mutating controller
   methods.
3. Pass `user.id()` into the service and enforce ownership there.
4. Keep JWT/security mechanics under `auth/security`.

Changing event search:

1. Start in `event/repository/EventSearchRepositoryImpl.java`.
2. Keep native SQL parameterized.
3. Add new sortable fields to `SORT_COLUMNS`.
4. Check count query and content query stay semantically aligned.
5. Consider indexes or generated data in a Flyway migration for new filters.

## Known Product/Architecture Decisions

- `Event.thumbnail` is a bare UUID because there is no files table yet.
- `owner_id` fields are soft references to `users(id)`, not foreign keys yet.
- `Schedule.event_id` is nullable to support global or not-yet-associated
  schedules.
- Events, categories, and URLs use soft delete.
- Text search favors PostgreSQL full-text search, including Spanish support.

## Watch Outs

- `EventUpdateRequest` treats `null` as "leave unchanged", so there is currently
  no way to clear nullable fields through update without changing the API shape.
- Category and URL deletes are soft deletes but blocked when referenced by
  active events.
- Metrics use `LocalDate.now()` with the JVM default timezone. Be careful if
  timezone-specific metric days become a product requirement.
- The local `test` profile points at PostgreSQL on `localhost:5432/kedada_test`;
  it is not an embedded database.
- This workspace currently has JDK 21, while the project targets Java 25. Use a
  JDK 25 for normal verification, or `mvn test -Djava.version=21` only as a
  temporary compile smoke test.
- Existing migrations and README text are Spanish-oriented; keep user-facing API
  examples consistent with the project language unless asked otherwise.
