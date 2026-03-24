package br.com.alr.api.sbkafkaproducersample.application.port.in;

import br.com.alr.api.sbkafkaproducersample.domain.model.Order;

import java.util.List;

public interface ListOrdersUseCase {

  List<Order> findAll();
}
