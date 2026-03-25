package br.com.alr.api.sbkafkaproducersample.adapter.out.persistence;

import br.com.alr.api.sbkafkaproducersample.adapter.out.persistence.entity.OrderJpaEntity;
import br.com.alr.api.sbkafkaproducersample.adapter.out.persistence.entity.ProductJpaEntity;
import br.com.alr.api.sbkafkaproducersample.adapter.out.persistence.entity.UserJpaEntity;
import br.com.alr.api.sbkafkaproducersample.adapter.out.persistence.mapper.PersistenceMapper;
import br.com.alr.api.sbkafkaproducersample.adapter.out.persistence.repository.OrderJpaRepository;
import br.com.alr.api.sbkafkaproducersample.adapter.out.persistence.repository.ProductJpaRepository;
import br.com.alr.api.sbkafkaproducersample.adapter.out.persistence.repository.UserJpaRepository;
import br.com.alr.api.sbkafkaproducersample.application.exception.NotFoundException;
import br.com.alr.api.sbkafkaproducersample.application.port.out.OrderPersistencePort;
import br.com.alr.api.sbkafkaproducersample.domain.enumtype.OrderStatus;
import br.com.alr.api.sbkafkaproducersample.domain.model.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class OrderPersistenceAdapter implements OrderPersistencePort {

  private final OrderJpaRepository orderJpaRepository;
  private final UserJpaRepository userJpaRepository;
  private final ProductJpaRepository productJpaRepository;
  private final PersistenceMapper persistenceMapper;

  public OrderPersistenceAdapter(
      OrderJpaRepository orderJpaRepository,
      UserJpaRepository userJpaRepository,
      ProductJpaRepository productJpaRepository,
      PersistenceMapper persistenceMapper
  ) {
    this.orderJpaRepository = orderJpaRepository;
    this.userJpaRepository = userJpaRepository;
    this.productJpaRepository = productJpaRepository;
    this.persistenceMapper = persistenceMapper;
  }

  @Override
  public Order save(Order order) {
    UserJpaEntity user = userJpaRepository.findById(order.userId())
        .orElseThrow(() -> new NotFoundException("User not found for id " + order.userId()));
    Map<Long, ProductJpaEntity> products = productJpaRepository.findByIdIn(
            order.items().stream().map(item -> item.productId()).toList())
        .stream()
        .collect(Collectors.toMap(ProductJpaEntity::getId, Function.identity()));

    return persistenceMapper.toDomain(
        orderJpaRepository.save(persistenceMapper.toEntity(order, user, products))
    );
  }

  @Override
  public List<Order> findAll() {
    return orderJpaRepository.findAll().stream().map(persistenceMapper::toDomain).toList();
  }

  @Override
  public Optional<Order> findById(Long id) {
    return orderJpaRepository.findById(id).map(persistenceMapper::toDomain);
  }

  @Transactional
  @Override
  public void updateStatus(Long orderId, OrderStatus status) {
    OrderJpaEntity entity = orderJpaRepository.findById(orderId)
        .orElseThrow(() -> new NotFoundException("Order not found for id " + orderId));
    entity.setStatus(status);
  }
}
