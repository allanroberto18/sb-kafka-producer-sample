package br.com.alr.api.sbkafkaproducersample.adapter.in.web;

import br.com.alr.api.sbkafkaproducersample.adapter.in.web.response.OutboxEventResponse;
import br.com.alr.api.sbkafkaproducersample.application.port.in.ListOutboxEventsUseCase;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/outbox-events")
public class OutboxEventController {

  private final ListOutboxEventsUseCase listOutboxEventsUseCase;
  private final ApiMapper apiMapper;

  public OutboxEventController(ListOutboxEventsUseCase listOutboxEventsUseCase, ApiMapper apiMapper) {
    this.listOutboxEventsUseCase = listOutboxEventsUseCase;
    this.apiMapper = apiMapper;
  }

  @Operation(summary = "List outbox events")
  @GetMapping
  public List<OutboxEventResponse> findAll() {
    return listOutboxEventsUseCase.findAll().stream().map(apiMapper::toResponse).toList();
  }
}
