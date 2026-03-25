package br.com.alr.api.sbkafkaproducersample.application.usecase;

import br.com.alr.api.sbkafkaproducersample.application.port.out.InvoiceEmailPort;
import br.com.alr.api.sbkafkaproducersample.application.port.out.OutboxEventPort;
import br.com.alr.api.sbkafkaproducersample.domain.enumtype.OrderStatus;
import br.com.alr.api.sbkafkaproducersample.domain.enumtype.OutboxStatus;
import br.com.alr.api.sbkafkaproducersample.domain.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateInvoiceEmailFromOrderCreatedEventServiceTest {

  @Mock
  private LoadOrderUserService loadOrderUserService;
  @Mock
  private InvoiceEmailPort invoiceEmailPort;
  @Mock
  private OutboxEventPort outboxEventPort;

  private CreateInvoiceEmailFromOrderCreatedEventService service;

  @BeforeEach
  void setUp() {
    service = new CreateInvoiceEmailFromOrderCreatedEventService(
        loadOrderUserService,
        new InvoiceEmailBodyFactory(),
        invoiceEmailPort,
        outboxEventPort,
        new ObjectMapper()
    );
  }

  @Test
  void shouldCreateInvoiceEmailAndEmailOutboxEvent() throws Exception {
    OrderCreatedEvent event = new OrderCreatedEvent(
        99L,
        1L,
        "Default User",
        OrderStatus.CREATED,
        new BigDecimal("70.90"),
        OffsetDateTime.parse("2026-03-24T12:00:00Z"),
        List.of(new OrderCreatedItemEvent(10L, "Notebook", 2, new BigDecimal("12.50"), new BigDecimal("25.00")))
    );
    UUID invoiceEmailId = UUID.fromString("22222222-2222-2222-2222-222222222222");

    when(loadOrderUserService.loadById(1L)).thenReturn(new User(1L, "Default User", "default.user@example.com"));
    when(invoiceEmailPort.save(any(InvoiceEmail.class))).thenAnswer(invocation -> {
      InvoiceEmail invoiceEmail = invocation.getArgument(0, InvoiceEmail.class);
      return new InvoiceEmail(
          invoiceEmailId,
          invoiceEmail.userId(),
          invoiceEmail.orderId(),
          invoiceEmail.recipientEmail(),
          invoiceEmail.subject(),
          invoiceEmail.body(),
          invoiceEmail.createdAt(),
          invoiceEmail.sentAt()
      );
    });

    service.create(event);

    ArgumentCaptor<InvoiceEmail> invoiceEmailCaptor = ArgumentCaptor.forClass(InvoiceEmail.class);
    verify(invoiceEmailPort).save(invoiceEmailCaptor.capture());
    assertThat(invoiceEmailCaptor.getValue().recipientEmail()).isEqualTo("default.user@example.com");
    assertThat(invoiceEmailCaptor.getValue().body()).contains("Hello Default User");

    ArgumentCaptor<OutboxEvent> outboxCaptor = ArgumentCaptor.forClass(OutboxEvent.class);
    verify(outboxEventPort).save(outboxCaptor.capture());
    assertThat(outboxCaptor.getValue().eventType()).isEqualTo(OutboxEventType.EMAIL_INVOICE_REQUESTED);
    assertThat(outboxCaptor.getValue().aggregateType()).isEqualTo(OutboxAggregateType.INVOICE_EMAIL);
    assertThat(outboxCaptor.getValue().status()).isEqualTo(OutboxStatus.PENDING);

    InvoiceEmailOutboxPayload payload = new ObjectMapper().readValue(outboxCaptor.getValue().payload(), InvoiceEmailOutboxPayload.class);
    assertThat(payload.invoiceEmailId()).isEqualTo(invoiceEmailId);
  }
}
