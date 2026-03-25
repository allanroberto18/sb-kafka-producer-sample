package br.com.alr.api.sbkafkaproducersample.domain.model;

import java.util.UUID;

public record InvoiceEmailOutboxPayload(UUID invoiceEmailId) {
}
