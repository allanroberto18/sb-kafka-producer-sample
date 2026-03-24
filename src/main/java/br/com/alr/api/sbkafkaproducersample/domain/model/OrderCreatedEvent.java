package br.com.alr.api.sbkafkaproducersample.domain.model;

import br.com.alr.api.sbkafkaproducersample.domain.enumtype.OrderStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public record OrderCreatedEvent(
    Long orderId,
    Long userId,
    String userName,
    OrderStatus status,
    BigDecimal totalAmount,
    OffsetDateTime createdAt,
    List<OrderCreatedItemEvent> items
) {
}
