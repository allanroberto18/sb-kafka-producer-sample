package br.com.alr.api.sbkafkaproducersample.application.port.in;

import br.com.alr.api.sbkafkaproducersample.application.dto.CreateOrderCommand;
import br.com.alr.api.sbkafkaproducersample.domain.model.Order;

import java.util.concurrent.CompletableFuture;

public interface CreateOrderUseCase {

  CompletableFuture<Order> create(CreateOrderCommand command);
}
