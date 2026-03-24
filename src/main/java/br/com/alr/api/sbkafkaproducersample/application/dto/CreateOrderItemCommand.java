package br.com.alr.api.sbkafkaproducersample.application.dto;

public record CreateOrderItemCommand(Long productId, Integer quantity) {
}
