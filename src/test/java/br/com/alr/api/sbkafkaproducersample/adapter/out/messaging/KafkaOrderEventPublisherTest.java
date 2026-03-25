package br.com.alr.api.sbkafkaproducersample.adapter.out.messaging;

import br.com.alr.api.sbkafkaproducersample.domain.enumtype.OrderStatus;
import br.com.alr.api.sbkafkaproducersample.domain.enumtype.OutboxStatus;
import br.com.alr.api.sbkafkaproducersample.domain.model.OrderCreatedEvent;
import br.com.alr.api.sbkafkaproducersample.domain.model.OrderCreatedItemEvent;
import br.com.alr.api.sbkafkaproducersample.domain.model.OutboxEvent;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.KafkaTemplate;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class KafkaOrderEventPublisherTest {

  @SuppressWarnings("unchecked")
  @Test
  void shouldDeserializeOutboxPayloadAndPublishTypedEvent() throws Exception {
    KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate = mock(KafkaTemplate.class);
    ObjectMapper objectMapper = new ObjectMapper();
    KafkaOrderEventPublisher publisher = new KafkaOrderEventPublisher(kafkaTemplate, objectMapper, "orders.created");

    OrderCreatedEvent payload = new OrderCreatedEvent(
        99L,
        1L,
        "User",
        OrderStatus.CREATED,
        new BigDecimal("70.90"),
        OffsetDateTime.parse("2026-03-24T10:15:30Z"),
        List.of(new OrderCreatedItemEvent(10L, "Notebook", 2, new BigDecimal("12.50"), new BigDecimal("25.00")))
    );

    OutboxEvent outboxEvent = new OutboxEvent(
        UUID.randomUUID(),
        "ORDER",
        "99",
        "ORDER_CREATED",
        objectMapper.writeValueAsString(payload),
        OutboxStatus.PENDING,
        OffsetDateTime.now(),
        null,
        null
    );

    when(kafkaTemplate.send(eq("orders.created"), eq("99"), eq(payload))).thenReturn(CompletableFuture.completedFuture(null));

    publisher.dispatch(outboxEvent);

    verify(kafkaTemplate).send("orders.created", "99", payload);
  }
}
