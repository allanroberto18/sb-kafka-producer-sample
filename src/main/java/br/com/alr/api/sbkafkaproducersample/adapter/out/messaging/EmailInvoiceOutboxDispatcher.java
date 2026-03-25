package br.com.alr.api.sbkafkaproducersample.adapter.out.messaging;

import br.com.alr.api.sbkafkaproducersample.application.exception.NotFoundException;
import br.com.alr.api.sbkafkaproducersample.application.port.out.EmailSenderPort;
import br.com.alr.api.sbkafkaproducersample.application.port.out.InvoiceEmailPort;
import br.com.alr.api.sbkafkaproducersample.application.port.out.OrderPersistencePort;
import br.com.alr.api.sbkafkaproducersample.application.port.out.OutboxEventDispatcherPort;
import br.com.alr.api.sbkafkaproducersample.domain.enumtype.OrderStatus;
import br.com.alr.api.sbkafkaproducersample.domain.model.InvoiceEmail;
import br.com.alr.api.sbkafkaproducersample.domain.model.InvoiceEmailOutboxPayload;
import br.com.alr.api.sbkafkaproducersample.domain.model.OutboxEvent;
import br.com.alr.api.sbkafkaproducersample.domain.model.OutboxEventType;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Component
public class EmailInvoiceOutboxDispatcher implements OutboxEventDispatcherPort {

  private final ObjectMapper objectMapper;
  private final InvoiceEmailPort invoiceEmailPort;
  private final EmailSenderPort emailSenderPort;
  private final OrderPersistencePort orderPersistencePort;

  public EmailInvoiceOutboxDispatcher(
      ObjectMapper objectMapper,
      InvoiceEmailPort invoiceEmailPort,
      EmailSenderPort emailSenderPort,
      OrderPersistencePort orderPersistencePort
  ) {
    this.objectMapper = objectMapper;
    this.invoiceEmailPort = invoiceEmailPort;
    this.emailSenderPort = emailSenderPort;
    this.orderPersistencePort = orderPersistencePort;
  }

  @Override
  public boolean supports(String eventType) {
    return OutboxEventType.EMAIL_INVOICE_REQUESTED.equals(eventType);
  }

  @Transactional
  @Override
  public void dispatch(OutboxEvent event) {
    InvoiceEmailOutboxPayload payload = deserialize(event.payload());
    InvoiceEmail invoiceEmail = invoiceEmailPort.findById(payload.invoiceEmailId())
        .orElseThrow(() -> new NotFoundException("Invoice email not found for id " + payload.invoiceEmailId()));
    emailSenderPort.send(invoiceEmail);
    invoiceEmailPort.markSent(invoiceEmail.id());
    orderPersistencePort.updateStatus(invoiceEmail.orderId(), OrderStatus.INVOICE_DELIVERED);
  }

  private InvoiceEmailOutboxPayload deserialize(String payload) {
    try {
      return objectMapper.readValue(payload, InvoiceEmailOutboxPayload.class);
    } catch (JacksonException exception) {
      throw new IllegalStateException("Could not deserialize invoice email outbox payload", exception);
    }
  }
}
