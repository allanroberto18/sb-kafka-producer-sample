package br.com.alr.api.sbkafkaproducersample.adapter.in.web.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CreateOrderRequest(
    @NotNull(message = "userId is required") Long userId,
    @NotEmpty(message = "items must not be empty") List<@Valid CreateOrderItemRequest> items
) {
}
