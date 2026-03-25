package br.com.alr.api.sbkafkaproducersample.domain.model;

import java.time.OffsetDateTime;
import java.util.UUID;

public record InvoiceEmail(
    UUID id,
    Long userId,
    Long orderId,
    String recipientEmail,
    String subject,
    String body,
    OffsetDateTime createdAt,
    OffsetDateTime sentAt
) {
}
