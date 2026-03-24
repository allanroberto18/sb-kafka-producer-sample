package br.com.alr.api.sbkafkaproducersample.adapter.in.web.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CreateOrderItemRequest(
    @NotNull(message = "productId is required") Long productId,
    @NotNull(message = "quantity is required") @Min(value = 1, message = "quantity must be greater than zero") Integer quantity
) {
}
