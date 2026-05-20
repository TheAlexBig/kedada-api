# Kedada API

Backend inicial de Kedada, una API REST para registrar, consultar, buscar y organizar eventos en El Salvador.

## Arquitectura

El proyecto usa Spring Boot 4, Java 25, Maven, PostgreSQL, Spring Web MVC, Spring Data JPA, Bean Validation, Flyway y springdoc-openapi.

La estructura sigue capas por dominio:

- `controller`: endpoints REST y validacion de entrada.
- `service`: reglas de negocio, transacciones y validacion de referencias.
- `repository`: acceso a datos JPA y queries nativas.
- `entity`: modelo persistente JPA.
- `dto`: contratos publicos de request/response.
- `mapper`: conversion entity/DTO.
- `common`: errores, respuestas compartidas y validaciones.

## Decisiones tecnicas

- `Event.thumbnail` se mantiene como `UUID` simple porque aun no existe tabla de archivos.
- `owner_id` se modela como soft reference `UUID`, sin foreign key, para permitir integrar JWT/Auth mas adelante.
- `Schedule` en el modelo original no tenia `event_id`. Se agrego `event_id uuid references events(id)` nullable para permitir horarios globales o aun no asociados, sin bloquear la evolucion del modelo.
- `EventMetricDaily` usa llave compuesta `(event_id, day)` para metricas diarias por evento.
- Los incrementos de vistas y compartidos usan `insert ... on conflict do update`, seguro frente a concurrencia en PostgreSQL.
- Los eventos, categorias y URLs usan soft delete con `is_deleted` y `deleted_at`; los listados normales no retornan eliminados.
- No se permite eliminar logicamente una categoria o URL mientras exista un evento activo que la referencie.
- La busqueda de texto usa `search_vector` con trigger e indice GIN.

## Arbol principal

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

## Correr localmente

```bash
docker compose up -d
mvn spring-boot:run
```

Swagger UI:

```text
http://localhost:8080/swagger-ui.html
```

## Ejemplos JSON

Crear categoria:

```json
{
  "name": "Musica",
  "ownerId": "11111111-1111-1111-1111-111111111111",
  "type": ["concert", "festival"]
}
```

Crear URL:

```json
{
  "url": "https://example.com/evento",
  "description": "Sitio oficial",
  "ownerId": "11111111-1111-1111-1111-111111111111",
  "kind": "official"
}
```

Crear evento:

```json
{
  "title": "Festival en San Salvador",
  "description": "Evento cultural abierto al publico.",
  "priority": 1,
  "thumbnail": null,
  "price": 12.50,
  "siteUrlId": "22222222-2222-2222-2222-222222222222",
  "referenceUrlId": null,
  "categoryId": "33333333-3333-3333-3333-333333333333"
}
```

Crear horario:

```json
{
  "eventId": "44444444-4444-4444-4444-444444444444",
  "startDate": "2026-06-01T19:00:00-06:00",
  "endDate": "2026-06-01T22:00:00-06:00",
  "ownerId": "11111111-1111-1111-1111-111111111111"
}
```

Registrar vista o compartido:

```bash
curl -X POST "http://localhost:8080/api/v1/events/{eventId}/view?ownerId=11111111-1111-1111-1111-111111111111"
curl -X POST "http://localhost:8080/api/v1/events/{eventId}/share?ownerId=11111111-1111-1111-1111-111111111111"
```

Buscar eventos:

```text
GET /api/v1/events?q=festival&categoryId={uuid}&minPrice=0&maxPrice=25&priority=1&fromDate=2026-06-01T00:00:00-06:00&page=0&size=20&sort=createdAt,desc
```

## Pendientes recomendados

- Agregar autenticacion y resolver `owner_id` desde JWT en vez de request/query param.
- Agregar pruebas de integracion con Testcontainers para PostgreSQL y Flyway.
- Afinar ranking de busqueda (`ts_rank`) cuando exista UX de resultados.
