package br.com.alr.api.sbkafkaproducersample.application.port.out;

import br.com.alr.api.sbkafkaproducersample.domain.model.OutboxEvent;

public interface OrderEventPublisherPort {

  void publish(OutboxEvent event);
}
