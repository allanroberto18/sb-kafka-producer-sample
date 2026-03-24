package br.com.alr.api.sbkafkaproducersample.application.port.out;

import br.com.alr.api.sbkafkaproducersample.domain.model.Order;

import java.util.List;

public interface OrderPersistencePort {

  Order save(Order order);

  List<Order> findAll();
}
