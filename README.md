# Order API Sample

Spring Boot 4.0.4 sample using Java 25, PostgreSQL, Kafka, Flyway, OpenAPI, Actuator, and the outbox pattern in a
hexagonal architecture.

## Stack

- Java 25
- Spring Boot 4.0.4
- Spring Web MVC
- Spring Data JPA
- Spring Validation
- PostgreSQL
- Apache Kafka
- Flyway
- Springdoc OpenAPI
- Actuator
- Lombok

## Architecture

The project is organized in hexagonal layers:

- `domain`: core business models and enums.
- `application`: input/output ports, use cases, commands, and business exceptions.
- `adapter.in.web`: REST controllers, request validation, response mapping, and `ProblemDetail` exception handling.
- `adapter.out.persistence`: JPA entities, Spring Data repositories, adapters, fixtures, and mapping.
- `adapter.out.messaging`: Kafka publishing and outbox polling.
- `config`: infrastructure configuration.

## How to start

1. Start PostgreSQL, Kafka, and Kafka UI:

```bash
docker compose up -d
```

2. Run the application:

```bash
mvn spring-boot:run
```

3. Open the main endpoints:

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI docs: `http://localhost:8080/v3/api-docs`
- Health: `http://localhost:8080/actuator/health`
- Kafka UI: `http://localhost:8081`

## Environment variables

The application imports `.env` automatically.

```env
DB_URL=jdbc:postgresql://localhost:5432/orders_db
DB_USERNAME=postgres
DB_PASSWORD=postgres
KAFKA_BOOTSTRAP_SERVERS=localhost:9092
KAFKA_ORDER_TOPIC=orders.created
OUTBOX_BATCH_SIZE=20
OUTBOX_FIXED_DELAY_MS=3000
```

## Default data fixtures

On startup the application inserts default data only when the target tables are empty:

- User: `Default User / default.user@example.com`
- Products: `Notebook`, `Keyboard`, `Mouse`

## Main API endpoints

- `GET /api/users`
- `GET /api/products`
- `GET /api/orders`
- `POST /api/orders`
- `GET /api/outbox-events`

### Example order request

```json
{
  "userId": 1,
  "items": [
    {
      "productId": 1,
      "quantity": 2
    },
    {
      "productId": 2,
      "quantity": 1
    }
  ]
}
```

The order endpoint is asynchronous. It stores the order and an outbox event in the same transaction. A scheduled
publisher reads pending outbox events and sends them to Kafka.

## Kafka UI

Kafka UI is available at `http://localhost:8081`.

Use it to inspect:

- topics such as `orders.created`
- produced messages and payloads
- consumer groups

If Kafka UI shows the cluster as offline after changing the Docker setup, recreate the containers:

```bash
docker compose down
docker compose up -d
```

## Tests

Run the test suite with:

```bash
mvn test
```

The integration tests use PostgreSQL via Testcontainers, so Docker must be running before executing the test suite.
