# Order API Sample

Spring Boot 4.0.4 sample using Java 25, PostgreSQL, Kafka, Mailpit, Flyway, OpenAPI, Actuator, and the outbox pattern
in a hexagonal architecture.

## Stack

- Java 25
- Spring Boot 4.0.4
- Spring Web MVC
- Spring Data JPA
- Spring Validation
- PostgreSQL
- Apache Kafka
- Spring Mail
- Mailpit
- Flyway
- Springdoc OpenAPI
- Actuator
- Lombok

## Architecture

The project is organized in hexagonal layers:

- `domain`: core business models and enums.
- `application`: input/output ports, use cases, commands, and business exceptions.
- `adapter.in.web`: REST controllers, request validation, response mapping, and `ProblemDetail` exception handling.
- `adapter.in.messaging`: Kafka consumer for order-created events.
- `adapter.out.persistence`: JPA entities, Spring Data repositories, adapters, fixtures, and mapping.
- `adapter.out.messaging`: Kafka publishing, SMTP email delivery, and outbox polling.
- `config`: infrastructure configuration.

## Event flow

The application runs two asynchronous stages on top of the same outbox mechanism:

1. `POST /api/orders` stores the order and an `ORDER_CREATED` outbox event in the same transaction.
2. `OutboxPublisher` publishes that event to Kafka.
3. `OrderCreatedInvoiceConsumer` consumes the Kafka message and creates an invoice email plus a second
   `EMAIL_INVOICE_REQUESTED` outbox event.
4. `OutboxPublisher` dispatches the email event through SMTP and marks the order as `INVOICE_DELIVERED`.

This keeps database writes, Kafka publishing, and email delivery decoupled while preserving retryable state in the
`outbox_events` table.

## How to start

1. Start PostgreSQL, Kafka, Kafka UI, and Mailpit:

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
- Mailpit UI: `http://localhost:8025`

## Environment variables

The application imports `.env` automatically.

```env
DB_URL=jdbc:postgresql://localhost:5432/orders_db
DB_USERNAME=postgres
DB_PASSWORD=postgres
KAFKA_BOOTSTRAP_SERVERS=localhost:9092
KAFKA_ORDER_TOPIC=orders.created
KAFKA_INVOICE_CONSUMER_GROUP_ID=invoice-email-consumer
MAIL_HOST=localhost
MAIL_PORT=1025
MAIL_SMTP_AUTH=false
MAIL_SMTP_STARTTLS=false
APP_MAIL_FROM=billing@example.com
OUTBOX_BATCH_SIZE=20
OUTBOX_FIXED_DELAY_MS=3000
OUTBOX_MAX_ATTEMPTS=5
```

Optional runtime switches:

- `app.kafka.invoice-consumer-enabled=true`
- `app.outbox.publisher.enabled=true`

## Database schema

Flyway migrations create:

- catalog and order tables
- `outbox_events` with retry metadata (`attempt_count`, status, error message, timestamps)
- `invoice_emails` for generated email payloads and delivery tracking

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

The order endpoint is asynchronous and returns `202 Accepted`. After an order is created you can inspect:

- Kafka messages in Kafka UI
- pending and published events at `GET /api/outbox-events`
- delivered emails in Mailpit
- order status transitions through `GET /api/orders`

## Local observability

Kafka UI is available at `http://localhost:8081`.

Use it to inspect:

- topics such as `orders.created`
- produced messages and payloads
- consumer groups

Mailpit is available at `http://localhost:8025`.

Use it to inspect:

- captured emails sent by the SMTP adapter
- invoice subject/body generated from consumed order events

If Kafka UI or Mailpit behaves inconsistently after changing the Docker setup, recreate the containers:

```bash
docker compose down
docker compose up -d
```

## Tests

Run the test suite with:

```bash
mvn test
```

The integration tests use PostgreSQL and Kafka via Testcontainers, so Docker must be running before executing the test
suite.
