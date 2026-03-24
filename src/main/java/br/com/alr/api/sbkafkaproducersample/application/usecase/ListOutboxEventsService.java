package br.com.alr.api.sbkafkaproducersample.application.usecase;

import br.com.alr.api.sbkafkaproducersample.application.port.in.ListOutboxEventsUseCase;
import br.com.alr.api.sbkafkaproducersample.application.port.out.OutboxEventPort;
import br.com.alr.api.sbkafkaproducersample.domain.model.OutboxEvent;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ListOutboxEventsService implements ListOutboxEventsUseCase {

  private final OutboxEventPort outboxEventPort;

  public ListOutboxEventsService(OutboxEventPort outboxEventPort) {
    this.outboxEventPort = outboxEventPort;
  }

  @Override
  public List<OutboxEvent> findAll() {
    return outboxEventPort.findAll();
  }
}
