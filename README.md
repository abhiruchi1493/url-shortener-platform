# URL Shortener Platform

Two Spring Boot (Kotlin) microservices, each built with **hexagonal architecture
(ports & adapters)**, PostgreSQL/JPA for persistence, and Kafka for inter-service
communication.

```
┌─────────────────────┐        click event (Kafka)        ┌─────────────────────┐
│ url-shortener-service│ ───────────────────────────────▶ │  analytics-service   │
│  (create + redirect) │     topic: url.click-events       │  (click tracking)    │
│  PostgreSQL: url_short│                                   │  PostgreSQL: analytics│
└─────────────────────┘                                    └─────────────────────┘
```

- **url-shortener-service** — owns short-code creation and redirect resolution.
  On every redirect it publishes a `ClickEvent` and moves on; it never waits
  on analytics.
- **analytics-service** — consumes `ClickEvent`s asynchronously and exposes
  `GET /api/v1/stats/{shortCode}`.

Each service has its own database, its own Gradle module, and its own
deployable JAR/Docker image — they only share the `common-events` module,
which holds the wire-format DTO for the Kafka contract. Nothing else is shared,
so the two can be deployed, scaled, and evolved independently.

## Why hexagonal architecture

Every service is structured the same way:

```
src/main/kotlin/com/shortener/<service>/
├── domain/
│   ├── model/        # Aggregates & value objects — plain Kotlin, no annotations
│   ├── exception/     # Domain exceptions
│   ├── port/in/       # Use case interfaces (driving/primary ports)
│   ├── port/out/      # Repository/publisher interfaces (driven/secondary ports)
│   └── service/        # Application service implementing the "in" ports,
│                       # depending only on the "out" port interfaces
├── adapter/
│   ├── in/web/         # @RestController — translates HTTP <-> use case calls
│   ├── in/messaging/    # @KafkaListener — translates Kafka <-> use case calls
│   └── out/persistence/ # @Component implementing a repository port via JPA
│   └── out/messaging/   # @Component implementing a publisher port via Kafka
└── config/             # @Configuration — composition root wiring the
                        # framework-free application service to its adapters
```

Rules enforced by this layout:

1. **`domain/` never imports Spring, JPA, or Kafka.** `UrlShortenerService` and
   `ClickAnalyticsService` are plain Kotlin classes — fully unit-testable with
   mocked ports and no Spring context (see the tests in `src/test`, they run in
   milliseconds, no `@SpringBootTest`).
2. **Dependencies point inward.** Adapters depend on domain ports; the domain
   never depends on adapters. Swapping Postgres/JPA for DynamoDB, or Kafka for
   SQS, only touches the `adapter/out/*` package.
3. **The JPA entity is not the domain model.** `ShortUrlJpaEntity` exists only
   inside `adapter/out/persistence`; the persistence adapter maps it to/from the
   `ShortUrl` aggregate. The domain stays free to enforce its own invariants
   (URL validation, short-code format, expiry) independent of how rows are
   stored.
4. **Wiring lives in `config/`,** not in the domain — application services are
   built via `@Bean` factory methods, so they carry zero framework annotations.

## API

### url-shortener-service (port 8081)

```
POST /api/v1/short-urls
{
  "originalUrl": "https://example.com/some/very/long/path",
  "customAlias": "my-link",      // optional
  "ttlSeconds": 86400            // optional
}
→ 201 { "shortCode": "my-link", "originalUrl": "...", "shortUrl": "http://localhost:8081/my-link" }

GET /{shortCode}
→ 302 redirect to the original URL (publishes a ClickEvent to Kafka)
```

### analytics-service (port 8082)

```
GET /api/v1/stats/{shortCode}
→ 200 { "shortCode": "my-link", "totalClicks": 42, "lastClickAt": "..." }
```

## Running locally

```bash
docker compose up --build
```

This starts Postgres (with both `url_shortener` and `analytics` databases via
`infra/init-multi-db.sh`), Kafka/Zookeeper, and both services. Flyway runs the
migrations in `src/main/resources/db/migration` on startup for each service.

```bash
curl -X POST localhost:8081/api/v1/short-urls \
  -H 'Content-Type: application/json' \
  -d '{"originalUrl": "https://anthropic.com"}'

curl -i localhost:8081/<shortCode>          # redirects, fires a click event
curl localhost:8082/api/v1/stats/<shortCode> # eventually consistent click count
```

## Running tests

```bash
./gradlew test
```

Domain/application-service tests use MockK against the port interfaces only —
no Testcontainers needed for the core business logic. (For full adapter
coverage you'd add `@DataJpaTest`/Testcontainers-backed tests against the real
Postgres image and an embedded Kafka broker for the listener — omitted here to
keep the example focused, but the seams are already in place for it.)

## Notable design decisions

- **Short codes** are generated as random base62 strings with collision
  retry (max 5 attempts) rather than a counter/Snowflake ID, to avoid a
  shared sequence becoming a coupling point between replicas. Swap
  `Base62ShortCodeGenerator` for a Snowflake-based one behind the same port
  if you need monotonic, sortable codes instead.
- **Click tracking is fully decoupled from the redirect path.** The redirect
  returns to the user as soon as the click is recorded against the short URL
  itself; publishing to Kafka is fire-and-forget with logging on failure, so
  an analytics outage never breaks redirects (note: at-most-once delivery as
  written — switch to a transactional outbox if you need stronger guarantees).
- **Optimistic locking (`@Version`)** on `short_urls` guards the click-count
  increment against lost updates under concurrent redirects without taking a
  row lock on every request.
- **`ddl-auto: validate`** — schema is owned by Flyway migrations, not
  Hibernate, so production schema changes are explicit and reviewable.
