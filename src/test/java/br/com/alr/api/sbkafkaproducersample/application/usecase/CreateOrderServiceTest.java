package br.com.alr.api.sbkafkaproducersample.application.usecase;

import br.com.alr.api.sbkafkaproducersample.application.dto.CreateOrderCommand;
import br.com.alr.api.sbkafkaproducersample.application.dto.CreateOrderItemCommand;
import br.com.alr.api.sbkafkaproducersample.application.exception.BusinessValidationException;
import br.com.alr.api.sbkafkaproducersample.application.port.out.OrderPersistencePort;
import br.com.alr.api.sbkafkaproducersample.application.port.out.OutboxEventPort;
import br.com.alr.api.sbkafkaproducersample.domain.enumtype.OrderStatus;
import br.com.alr.api.sbkafkaproducersample.domain.model.Order;
import br.com.alr.api.sbkafkaproducersample.domain.model.OrderCreatedEvent;
import br.com.alr.api.sbkafkaproducersample.domain.model.Product;
import br.com.alr.api.sbkafkaproducersample.domain.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateOrderServiceTest {

  @Mock
  private LoadOrderUserService loadOrderUserService;
  @Mock
  private LoadOrderProductsService loadOrderProductsService;
  @Mock
  private OrderPersistencePort orderPersistencePort;
  @Mock
  private OutboxEventPort outboxEventPort;

  private CreateOrderService createOrderService;

  @BeforeEach
  void setUp() {
    createOrderService = new CreateOrderService(
        loadOrderUserService,
        loadOrderProductsService,
        orderPersistencePort,
        outboxEventPort,
        new ObjectMapper()
    );
  }

  @Test
  void shouldCreateOrderAndOutboxEvent() {
    CreateOrderCommand command = new CreateOrderCommand(1L, List.of(
        new CreateOrderItemCommand(10L, 2),
        new CreateOrderItemCommand(20L, 1)
    ));

    when(loadOrderUserService.loadById(1L)).thenReturn(new User(1L, "User", "user@example.com"));
    when(loadOrderProductsService.loadProductsByItems(command.items())).thenReturn(Map.of(
        10L, new Product(10L, "Notebook", new BigDecimal("12.50")),
        20L, new Product(20L, "Keyboard", new BigDecimal("45.90"))
    ));
    when(orderPersistencePort.save(any(Order.class))).thenAnswer(invocation -> {
      Order order = invocation.getArgument(0, Order.class);
      return new Order(99L, order.userId(), order.userName(), OrderStatus.CREATED, order.totalAmount(), order.createdAt(), order.items());
    });

    Order order = createOrderService.create(command).join();

    assertThat(order.id()).isEqualTo(99L);
    assertThat(order.totalAmount()).isEqualByComparingTo("70.90");
    ArgumentCaptor<br.com.alr.api.sbkafkaproducersample.domain.model.OutboxEvent> captor = ArgumentCaptor.forClass(br.com.alr.api.sbkafkaproducersample.domain.model.OutboxEvent.class);
    verify(outboxEventPort, times(1)).save(captor.capture());

    OrderCreatedEvent event = new ObjectMapper().readValue(captor.getValue().payload(), OrderCreatedEvent.class);
    assertThat(event.orderId()).isEqualTo(99L);
    assertThat(event.userId()).isEqualTo(1L);
    assertThat(event.totalAmount()).isEqualByComparingTo("70.90");
    assertThat(event.items()).hasSize(2);
    assertThat(event.items().getFirst().productId()).isEqualTo(10L);
  }

  @Test
  void shouldRejectDuplicatedProducts() {
    CreateOrderCommand command = new CreateOrderCommand(1L, List.of(
        new CreateOrderItemCommand(10L, 1),
        new CreateOrderItemCommand(10L, 2)
    ));

    assertThatThrownBy(() -> createOrderService.create(command).join())
        .isInstanceOf(BusinessValidationException.class)
        .hasMessageContaining("Duplicated products");
  }
}
