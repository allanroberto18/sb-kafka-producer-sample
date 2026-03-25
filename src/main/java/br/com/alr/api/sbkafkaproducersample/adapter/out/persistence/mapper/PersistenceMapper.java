package br.com.alr.api.sbkafkaproducersample.adapter.out.persistence.mapper;

import br.com.alr.api.sbkafkaproducersample.adapter.out.persistence.entity.*;
import br.com.alr.api.sbkafkaproducersample.domain.model.*;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
public class PersistenceMapper {

  private final ModelMapper modelMapper;

  public PersistenceMapper(ModelMapper modelMapper) {
    this.modelMapper = modelMapper;
  }

  public User toDomain(UserJpaEntity entity) {
    return modelMapper.map(entity, User.class);
  }

  public Product toDomain(ProductJpaEntity entity) {
    return modelMapper.map(entity, Product.class);
  }

  public Order toDomain(OrderJpaEntity entity) {
    return modelMapper.map(entity, Order.class);
  }

  public OrderItem toDomain(OrderItemJpaEntity entity) {
    return modelMapper.map(entity, OrderItem.class);
  }

  public OutboxEvent toDomain(OutboxEventJpaEntity entity) {
    return modelMapper.map(entity, OutboxEvent.class);
  }

  public InvoiceEmail toDomain(InvoiceEmailJpaEntity entity) {
    return new InvoiceEmail(
        entity.getId(),
        entity.getUser().getId(),
        entity.getOrder().getId(),
        entity.getRecipientEmail(),
        entity.getSubject(),
        entity.getBody(),
        entity.getCreatedAt(),
        entity.getSentAt()
    );
  }

  public OrderJpaEntity toEntity(Order order, UserJpaEntity user, java.util.Map<Long, ProductJpaEntity> productMap) {
    OrderJpaEntity entity = OrderJpaEntity.builder()
        .id(order.id())
        .user(user)
        .status(order.status())
        .totalAmount(order.totalAmount())
        .createdAt(order.createdAt())
        .items(new ArrayList<>())
        .build();

    order.items().forEach(item -> {
      OrderItemJpaEntity itemEntity = OrderItemJpaEntity.builder()
          .order(entity)
          .product(productMap.get(item.productId()))
          .quantity(item.quantity())
          .unitPrice(item.unitPrice())
          .lineTotal(item.lineTotal())
          .build();
      entity.getItems().add(itemEntity);
    });
    return entity;
  }

  public OutboxEventJpaEntity toEntity(OutboxEvent event) {
    return modelMapper.map(event, OutboxEventJpaEntity.class);
  }

  public InvoiceEmailJpaEntity toEntity(InvoiceEmail invoiceEmail, UserJpaEntity user, OrderJpaEntity order) {
    return InvoiceEmailJpaEntity.builder()
        .id(invoiceEmail.id())
        .user(user)
        .order(order)
        .recipientEmail(invoiceEmail.recipientEmail())
        .subject(invoiceEmail.subject())
        .body(invoiceEmail.body())
        .createdAt(invoiceEmail.createdAt())
        .sentAt(invoiceEmail.sentAt())
        .build();
  }
}
