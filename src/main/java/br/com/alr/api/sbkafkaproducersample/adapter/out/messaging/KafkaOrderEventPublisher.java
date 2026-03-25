package br.com.alr.api.sbkafkaproducersample.adapter.out.messaging;

import br.com.alr.api.sbkafkaproducersample.application.port.out.OutboxEventDispatcherPort;
import br.com.alr.api.sbkafkaproducersample.domain.model.OrderCreatedEvent;
import br.com.alr.api.sbkafkaproducersample.domain.model.OutboxEvent;
import br.com.alr.api.sbkafkaproducersample.domain.model.OutboxEventType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Component
public class KafkaOrderEventPublisher implements OutboxEventDispatcherPort {

  private final KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate;
  private final ObjectMapper objectMapper;
  private final String topic;

  public KafkaOrderEventPublisher(
      KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate,
      ObjectMapper objectMapper,
      @Value("${app.kafka.order-topic}") String topic
  ) {
    this.kafkaTemplate = kafkaTemplate;
    this.objectMapper = objectMapper;
    this.topic = topic;
  }

  @Override
  public boolean supports(String eventType) {
    return OutboxEventType.ORDER_CREATED.equals(eventType);
  }

  @Override
  public void dispatch(OutboxEvent event) {
    kafkaTemplate.send(topic, event.aggregateId(), deserialize(event.payload())).join();
  }

  private OrderCreatedEvent deserialize(String payload) {
    try {
      return objectMapper.readValue(payload, OrderCreatedEvent.class);
    } catch (JacksonException exception) {
      throw new IllegalStateException("Could not deserialize order outbox payload", exception);
    }
  }
}
