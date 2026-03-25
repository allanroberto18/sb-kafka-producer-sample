package br.com.alr.api.sbkafkaproducersample.adapter.out.messaging;

import br.com.alr.api.sbkafkaproducersample.application.port.out.OutboxEventDispatcherPort;
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
  private final java.util.List<OutboxEventDispatcherPort> dispatchers;
  private final int batchSize;
  private final int maxAttempts;

  public OutboxPublisher(
      OutboxEventPort outboxEventPort,
      java.util.List<OutboxEventDispatcherPort> dispatchers,
      @Value("${app.outbox.batch-size}") int batchSize,
      @Value("${app.outbox.max-attempts}") int maxAttempts
  ) {
    this.outboxEventPort = outboxEventPort;
    this.dispatchers = dispatchers;
    this.batchSize = batchSize;
    this.maxAttempts = maxAttempts;
  }

  @Scheduled(fixedDelayString = "${app.outbox.fixed-delay-ms}")
  public void publishPendingEvents() {
    for (OutboxEvent event : outboxEventPort.findProcessableEvents(batchSize, maxAttempts)) {
      try {
        resolveDispatcher(event.eventType()).dispatch(event);
        outboxEventPort.markPublished(event.id());
      } catch (Exception exception) {
        LOGGER.warn("Failed to publish outbox event {}", event.id(), exception);
        outboxEventPort.markFailed(event.id(), truncate(exception.getMessage()));
      }
    }
  }

  private OutboxEventDispatcherPort resolveDispatcher(String eventType) {
    return dispatchers.stream()
        .filter(dispatcher -> dispatcher.supports(eventType))
        .findFirst()
        .orElseThrow(() -> new IllegalStateException("No dispatcher found for outbox event type " + eventType));
  }

  private String truncate(String message) {
    if (message == null || message.length() <= 255) {
      return message;
    }
    return message.substring(0, 255);
  }
}
