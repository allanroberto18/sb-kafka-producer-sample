package br.com.alr.api.sbkafkaproducersample.adapter.in.web.response;

import java.math.BigDecimal;

public record OrderItemResponse(
    Long productId,
    String productName,
    Integer quantity,
    BigDecimal unitPrice,
    BigDecimal lineTotal
) {
}
