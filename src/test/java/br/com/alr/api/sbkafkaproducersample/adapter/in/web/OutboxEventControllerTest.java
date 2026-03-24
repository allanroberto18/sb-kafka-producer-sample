package br.com.alr.api.sbkafkaproducersample.adapter.in.web;

import br.com.alr.api.sbkafkaproducersample.application.port.in.ListOutboxEventsUseCase;
import br.com.alr.api.sbkafkaproducersample.domain.enumtype.OutboxStatus;
import br.com.alr.api.sbkafkaproducersample.domain.model.OutboxEvent;
import br.com.alr.api.sbkafkaproducersample.support.TestMappers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class OutboxEventControllerTest {

  private MockMvc mockMvc;
  private ListOutboxEventsUseCase listOutboxEventsUseCase;

  @BeforeEach
  void setUp() {
    listOutboxEventsUseCase = mock(ListOutboxEventsUseCase.class);
    mockMvc = MockMvcBuilders
        .standaloneSetup(new OutboxEventController(listOutboxEventsUseCase, TestMappers.apiMapper()))
        .setControllerAdvice(new RestControllerExceptionHandler())
        .build();
  }

  @Test
  void shouldListOutboxEvents() throws Exception {
    when(listOutboxEventsUseCase.findAll()).thenReturn(List.of(
        new OutboxEvent(
            UUID.fromString("11111111-1111-1111-1111-111111111111"),
            "ORDER",
            "1",
            "ORDER_CREATED",
            "{\"orderId\":1}",
            OutboxStatus.PENDING,
            OffsetDateTime.parse("2026-03-24T10:15:30Z"),
            null,
            null
        )
    ));

    mockMvc.perform(get("/api/outbox-events"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].aggregateType").value("ORDER"));
  }
}
