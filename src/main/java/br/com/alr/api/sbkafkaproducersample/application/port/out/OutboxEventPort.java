package br.com.alr.api.sbkafkaproducersample.application.port.out;

import br.com.alr.api.sbkafkaproducersample.domain.model.OutboxEvent;

import java.util.List;
import java.util.UUID;

public interface OutboxEventPort {

  OutboxEvent save(OutboxEvent event);

  List<OutboxEvent> findAll();

  List<OutboxEvent> findProcessableEvents(int limit, int maxAttempts);

  void markPublished(UUID eventId);

  void markFailed(UUID eventId, String errorMessage);
}
