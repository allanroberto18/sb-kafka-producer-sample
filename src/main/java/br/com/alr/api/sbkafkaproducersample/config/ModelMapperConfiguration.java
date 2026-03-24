package br.com.alr.api.sbkafkaproducersample.config;

import br.com.alr.api.sbkafkaproducersample.adapter.in.web.request.CreateOrderItemRequest;
import br.com.alr.api.sbkafkaproducersample.adapter.in.web.request.CreateOrderRequest;
import br.com.alr.api.sbkafkaproducersample.adapter.in.web.response.*;
import br.com.alr.api.sbkafkaproducersample.adapter.out.persistence.entity.*;
import br.com.alr.api.sbkafkaproducersample.application.dto.CreateOrderCommand;
import br.com.alr.api.sbkafkaproducersample.application.dto.CreateOrderItemCommand;
import br.com.alr.api.sbkafkaproducersample.domain.model.*;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ModelMapperConfiguration {

  @Bean
  public ModelMapper modelMapper() {
    ModelMapper modelMapper = new ModelMapper();
    modelMapper.getConfiguration()
        .setMatchingStrategy(MatchingStrategies.STRICT)
        .setSkipNullEnabled(true);

    modelMapper.createTypeMap(CreateOrderRequest.class, CreateOrderCommand.class).setConverter(createOrderRequestToCommand());
    modelMapper.createTypeMap(CreateOrderItemRequest.class, CreateOrderItemCommand.class).setConverter(createOrderItemRequestToCommand());
    modelMapper.createTypeMap(User.class, UserResponse.class).setConverter(userToResponse());
    modelMapper.createTypeMap(Product.class, ProductResponse.class).setConverter(productToResponse());
    modelMapper.createTypeMap(OrderItem.class, OrderItemResponse.class).setConverter(orderItemToResponse());
    modelMapper.createTypeMap(Order.class, OrderResponse.class).setConverter(orderToResponse());
    modelMapper.createTypeMap(OutboxEvent.class, OutboxEventResponse.class).setConverter(outboxToResponse());

    modelMapper.createTypeMap(UserJpaEntity.class, User.class).setConverter(userEntityToDomain());
    modelMapper.createTypeMap(ProductJpaEntity.class, Product.class).setConverter(productEntityToDomain());
    modelMapper.createTypeMap(OrderItemJpaEntity.class, OrderItem.class).setConverter(orderItemEntityToDomain());
    modelMapper.createTypeMap(OrderJpaEntity.class, Order.class).setConverter(orderEntityToDomain());
    modelMapper.createTypeMap(OutboxEventJpaEntity.class, OutboxEvent.class).setConverter(outboxEntityToDomain());
    modelMapper.createTypeMap(OutboxEvent.class, OutboxEventJpaEntity.class).setConverter(outboxDomainToEntity());

    return modelMapper;
  }

  private Converter<CreateOrderRequest, CreateOrderCommand> createOrderRequestToCommand() {
    return context -> new CreateOrderCommand(
        context.getSource().userId(),
        context.getSource().items().stream()
            .map(item -> context.getMappingEngine().map(context.create(item, CreateOrderItemCommand.class)))
            .toList()
    );
  }

  private Converter<CreateOrderItemRequest, CreateOrderItemCommand> createOrderItemRequestToCommand() {
    return context -> new CreateOrderItemCommand(context.getSource().productId(), context.getSource().quantity());
  }

  private Converter<User, UserResponse> userToResponse() {
    return context -> new UserResponse(context.getSource().id(), context.getSource().name(), context.getSource().email());
  }

  private Converter<Product, ProductResponse> productToResponse() {
    return context -> new ProductResponse(context.getSource().id(), context.getSource().name(), context.getSource().price());
  }

  private Converter<OrderItem, OrderItemResponse> orderItemToResponse() {
    return context -> new OrderItemResponse(
        context.getSource().productId(),
        context.getSource().productName(),
        context.getSource().quantity(),
        context.getSource().unitPrice(),
        context.getSource().lineTotal()
    );
  }

  private Converter<Order, OrderResponse> orderToResponse() {
    return context -> new OrderResponse(
        context.getSource().id(),
        context.getSource().userId(),
        context.getSource().userName(),
        context.getSource().status(),
        context.getSource().totalAmount(),
        context.getSource().createdAt(),
        context.getSource().items().stream()
            .map(item -> context.getMappingEngine().map(context.create(item, OrderItemResponse.class)))
            .toList()
    );
  }

  private Converter<OutboxEvent, OutboxEventResponse> outboxToResponse() {
    return context -> new OutboxEventResponse(
        context.getSource().id(),
        context.getSource().aggregateType(),
        context.getSource().aggregateId(),
        context.getSource().eventType(),
        context.getSource().payload(),
        context.getSource().status(),
        context.getSource().createdAt(),
        context.getSource().processedAt(),
        context.getSource().errorMessage()
    );
  }

  private Converter<UserJpaEntity, User> userEntityToDomain() {
    return context -> new User(context.getSource().getId(), context.getSource().getName(), context.getSource().getEmail());
  }

  private Converter<ProductJpaEntity, Product> productEntityToDomain() {
    return context -> new Product(context.getSource().getId(), context.getSource().getName(), context.getSource().getPrice());
  }

  private Converter<OrderItemJpaEntity, OrderItem> orderItemEntityToDomain() {
    return context -> new OrderItem(
        context.getSource().getId(),
        context.getSource().getProduct().getId(),
        context.getSource().getProduct().getName(),
        context.getSource().getQuantity(),
        context.getSource().getUnitPrice(),
        context.getSource().getLineTotal()
    );
  }

  private Converter<OrderJpaEntity, Order> orderEntityToDomain() {
    return context -> new Order(
        context.getSource().getId(),
        context.getSource().getUser().getId(),
        context.getSource().getUser().getName(),
        context.getSource().getStatus(),
        context.getSource().getTotalAmount(),
        context.getSource().getCreatedAt(),
        context.getSource().getItems().stream()
            .map(item -> context.getMappingEngine().map(context.create(item, OrderItem.class)))
            .toList()
    );
  }

  private Converter<OutboxEventJpaEntity, OutboxEvent> outboxEntityToDomain() {
    return context -> new OutboxEvent(
        context.getSource().getId(),
        context.getSource().getAggregateType(),
        context.getSource().getAggregateId(),
        context.getSource().getEventType(),
        context.getSource().getPayload(),
        context.getSource().getStatus(),
        context.getSource().getCreatedAt(),
        context.getSource().getProcessedAt(),
        context.getSource().getErrorMessage()
    );
  }

  private Converter<OutboxEvent, OutboxEventJpaEntity> outboxDomainToEntity() {
    return context -> {
      OutboxEvent source = context.getSource();
      OutboxEventJpaEntity entity = new OutboxEventJpaEntity();
      entity.setId(source.id());
      entity.setAggregateType(source.aggregateType());
      entity.setAggregateId(source.aggregateId());
      entity.setEventType(source.eventType());
      entity.setPayload(source.payload());
      entity.setStatus(source.status());
      entity.setCreatedAt(source.createdAt());
      entity.setProcessedAt(source.processedAt());
      entity.setErrorMessage(source.errorMessage());
      return entity;
    };
  }
}
