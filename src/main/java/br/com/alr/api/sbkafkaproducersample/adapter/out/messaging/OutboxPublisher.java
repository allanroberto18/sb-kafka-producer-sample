package br.com.alr.api.sbkafkaproducersample.adapter.out.messaging;

import br.com.alr.api.sbkafkaproducersample.application.port.out.OrderEventPublisherPort;
import br.com.alr.api.sbkafkaproducersample.application.port.out.OutboxEventPort;
import br.com.alr.api.sbkafkaproducersample.domain.model.OutboxEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@ConditionalOnProperty(value = "app.outbox.publisher.enabled", havingValue = "true", matchIfMissing = true)
@Component
public class OutboxPublisher {

  private static final Logger LOGGER = LoggerFactory.getLogger(OutboxPublisher.class);

  private final OutboxEventPort outboxEventPort;
  private final OrderEventPublisherPort orderEventPublisherPort;
  private final int batchSize;

  public OutboxPublisher(
      OutboxEventPort outboxEventPort,
      OrderEventPublisherPort orderEventPublisherPort,
      @Value("${app.outbox.batch-size}") int batchSize
  ) {
    this.outboxEventPort = outboxEventPort;
    this.orderEventPublisherPort = orderEventPublisherPort;
    this.batchSize = batchSize;
  }

  @Scheduled(fixedDelayString = "${app.outbox.fixed-delay-ms}")
  public void publishPendingEvents() {
    for (OutboxEvent event : outboxEventPort.findProcessableEvents(batchSize)) {
      try {
        orderEventPublisherPort.publish(event);
        outboxEventPort.markPublished(event.id());
      } catch (Exception exception) {
        LOGGER.warn("Failed to publish outbox event {}", event.id(), exception);
        outboxEventPort.markFailed(event.id(), truncate(exception.getMessage()));
      }
    }
  }

  private String truncate(String message) {
    if (message == null || message.length() <= 255) {
      return message;
    }
    return message.substring(0, 255);
  }
}
