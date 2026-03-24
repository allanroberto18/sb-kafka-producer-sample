package br.com.alr.api.sbkafkaproducersample.application.usecase;

import br.com.alr.api.sbkafkaproducersample.application.dto.CreateOrderCommand;
import br.com.alr.api.sbkafkaproducersample.application.dto.CreateOrderItemCommand;
import br.com.alr.api.sbkafkaproducersample.application.exception.BusinessValidationException;
import br.com.alr.api.sbkafkaproducersample.application.port.in.CreateOrderUseCase;
import br.com.alr.api.sbkafkaproducersample.application.port.out.OrderPersistencePort;
import br.com.alr.api.sbkafkaproducersample.application.port.out.OutboxEventPort;
import br.com.alr.api.sbkafkaproducersample.domain.enumtype.OrderStatus;
import br.com.alr.api.sbkafkaproducersample.domain.enumtype.OutboxStatus;
import br.com.alr.api.sbkafkaproducersample.domain.model.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
public class CreateOrderService implements CreateOrderUseCase {

  private final LoadOrderUserService loadOrderUserService;
  private final LoadOrderProductsService loadOrderProductsService;
  private final OrderPersistencePort orderPersistencePort;
  private final OutboxEventPort outboxEventPort;
  private final ObjectMapper objectMapper;

  public CreateOrderService(
      LoadOrderUserService loadOrderUserService,
      LoadOrderProductsService loadOrderProductsService,
      OrderPersistencePort orderPersistencePort,
      OutboxEventPort outboxEventPort,
      ObjectMapper objectMapper
  ) {
    this.loadOrderUserService = loadOrderUserService;
    this.loadOrderProductsService = loadOrderProductsService;
    this.orderPersistencePort = orderPersistencePort;
    this.outboxEventPort = outboxEventPort;
    this.objectMapper = objectMapper;
  }

    @Async("taskExecutor")
    @Transactional
    @Override
    public CompletableFuture<Order> create(CreateOrderCommand command) {
    validateDuplicatedProducts(command.items());

    User user = loadOrderUserService.loadById(command.userId());
    Map<Long, Product> productMap = loadOrderProductsService.loadProductsByItems(command.items());

    List<OrderItem> items = command.items().stream()
        .map(item -> toOrderItem(item, productMap.get(item.productId())))
        .toList();

    BigDecimal totalAmount = items.stream()
        .map(OrderItem::lineTotal)
        .reduce(BigDecimal.ZERO, BigDecimal::add);

    Order savedOrder = orderPersistencePort.save(new Order(
        null,
        user.id(),
        user.name(),
        OrderStatus.CREATED,
        totalAmount,
        OffsetDateTime.now(),
        items
    ));

    outboxEventPort.save(new OutboxEvent(
        UUID.randomUUID(),
        "ORDER",
        savedOrder.id().toString(),
        "ORDER_CREATED",
        createPayload(toOrderCreatedEvent(savedOrder)),
        OutboxStatus.PENDING,
        OffsetDateTime.now(),
        null,
        null
    ));

    return CompletableFuture.completedFuture(savedOrder);
  }

  private void validateDuplicatedProducts(List<CreateOrderItemCommand> items) {
    long distinctIds = items.stream().map(CreateOrderItemCommand::productId).distinct().count();
    if (distinctIds != items.size()) {
      throw new BusinessValidationException("Duplicated products are not allowed in the same order");
    }
  }

  private OrderItem toOrderItem(CreateOrderItemCommand item, Product product) {
    BigDecimal lineTotal = product.price().multiply(BigDecimal.valueOf(item.quantity()));
    return new OrderItem(
        null,
        product.id(),
        product.name(),
        item.quantity(),
        product.price(),
        lineTotal
    );
  }

  private OrderCreatedEvent toOrderCreatedEvent(Order order) {
    return new OrderCreatedEvent(
        order.id(),
        order.userId(),
        order.userName(),
        order.status(),
        order.totalAmount(),
        order.createdAt(),
        order.items().stream()
            .map(item -> new OrderCreatedItemEvent(
                item.productId(),
                item.productName(),
                item.quantity(),
                item.unitPrice(),
                item.lineTotal()
            ))
            .toList()
    );
  }

  private String createPayload(OrderCreatedEvent event) {
    try {
      return objectMapper.writeValueAsString(event);
    } catch (JacksonException exception) {
      throw new BusinessValidationException("Could not serialize order outbox payload");
    }
  }
}
