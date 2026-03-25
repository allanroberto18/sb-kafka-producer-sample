package br.com.alr.api.sbkafkaproducersample.application.port.out;

import br.com.alr.api.sbkafkaproducersample.domain.enumtype.OrderStatus;
import br.com.alr.api.sbkafkaproducersample.domain.model.Order;

import java.util.List;
import java.util.Optional;

public interface OrderPersistencePort {

  Order save(Order order);

  List<Order> findAll();

  Optional<Order> findById(Long id);

  void updateStatus(Long orderId, OrderStatus status);
}
