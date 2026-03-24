package br.com.alr.api.sbkafkaproducersample.domain.model;

import java.math.BigDecimal;

public record OrderCreatedItemEvent(
    Long productId,
    String productName,
    Integer quantity,
    BigDecimal unitPrice,
    BigDecimal lineTotal
) {
}
