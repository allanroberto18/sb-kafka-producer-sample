package br.com.alr.api.sbkafkaproducersample.domain.model;

import br.com.alr.api.sbkafkaproducersample.domain.enumtype.OutboxStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

public record OutboxEvent(
    UUID id,
    String aggregateType,
    String aggregateId,
    String eventType,
    String payload,
    OutboxStatus status,
    OffsetDateTime createdAt,
    OffsetDateTime processedAt,
    String errorMessage
) {
}
