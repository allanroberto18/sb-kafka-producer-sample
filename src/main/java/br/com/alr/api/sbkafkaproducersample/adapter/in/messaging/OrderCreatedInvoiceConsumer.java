package br.com.alr.api.sbkafkaproducersample.adapter.in.messaging;

import br.com.alr.api.sbkafkaproducersample.application.usecase.CreateInvoiceEmailFromOrderCreatedEventService;
import br.com.alr.api.sbkafkaproducersample.domain.model.OrderCreatedEvent;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@ConditionalOnProperty(value = "app.kafka.invoice-consumer-enabled", havingValue = "true", matchIfMissing = true)
@Component
public class OrderCreatedInvoiceConsumer {

  private final ObjectMapper objectMapper;
  private final CreateInvoiceEmailFromOrderCreatedEventService createInvoiceEmailFromOrderCreatedEventService;

  public OrderCreatedInvoiceConsumer(
      ObjectMapper objectMapper,
      CreateInvoiceEmailFromOrderCreatedEventService createInvoiceEmailFromOrderCreatedEventService
  ) {
    this.objectMapper = objectMapper;
    this.createInvoiceEmailFromOrderCreatedEventService = createInvoiceEmailFromOrderCreatedEventService;
  }

  @KafkaListener(
      topics = "${app.kafka.order-topic}",
      groupId = "${app.kafka.invoice-consumer-group-id}",
      autoStartup = "${app.kafka.invoice-consumer-enabled:true}"
  )
  public void consume(String payload) {
    createInvoiceEmailFromOrderCreatedEventService.create(deserialize(payload));
  }

  private OrderCreatedEvent deserialize(String payload) {
    try {
      return objectMapper.readValue(payload, OrderCreatedEvent.class);
    } catch (JacksonException exception) {
      throw new IllegalStateException("Could not deserialize consumed order event", exception);
    }
  }
}
