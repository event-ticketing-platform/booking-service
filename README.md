# Booking Service

Booking owns reservations, the local event catalog, and ticket inventory for this self-contained project.

## What It Does

- Lists active events and ticket types.
- Reserves and releases persisted ticket inventory.
- Creates bookings for authenticated customers.
- Starts payment through Payment Service.
- Consumes `PaymentCompleted` from RabbitMQ and confirms bookings asynchronously.
- Publishes `BookingConfirmed` and `BookingCancelled`.

## Main Endpoints

| Method | Endpoint | Purpose |
| --- | --- | --- |
| `GET` | `/events` | List active events with ticket types |
| `GET` | `/events/{eventId}` | Get one event |
| `GET` | `/events/{eventId}/ticket-types` | List ticket types for an event |
| `GET` | `/ticket-types/{ticketTypeId}` | Get one ticket type |
| `POST` | `/bookings` | Create a booking |
| `GET` | `/bookings/{id}` | Get one booking |
| `DELETE` | `/bookings/{id}` | Cancel a booking |
| `POST` | `/bookings/{id}/pay` | Start payment for a booking |
| `GET` | `/users/{userId}/bookings` | List bookings for one user |

Event and ticket catalog reads are public. Booking operations require a gateway-issued JWT.

## Run With The Full System

```bash
cd ../infra
docker compose up --build
```

Booking is exposed at:

```text
http://localhost:18081
```

## Important Environment Variables

| Variable | Default | Meaning |
| --- | --- | --- |
| `SERVER_PORT` | `8081` | Service port |
| `SPRING_DATASOURCE_URL` | local booking DB | PostgreSQL connection |
| `SPRING_DATASOURCE_USERNAME` | `booking_user` | DB user |
| `SPRING_DATASOURCE_PASSWORD` | `booking_pass` | DB password |
| `PAYMENT_SERVICE_URL` | `http://localhost:8082` | Payment Service URL |

## Tests

```bash
./mvnw test
```
