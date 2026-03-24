package br.com.alr.api.sbkafkaproducersample.application.dto;

import java.util.List;

public record CreateOrderCommand(Long userId, List<CreateOrderItemCommand> items) {
}
