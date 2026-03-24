package br.com.alr.api.sbkafkaproducersample.application.port.in;

import br.com.alr.api.sbkafkaproducersample.domain.model.OutboxEvent;

import java.util.List;

public interface ListOutboxEventsUseCase {

  List<OutboxEvent> findAll();
}
