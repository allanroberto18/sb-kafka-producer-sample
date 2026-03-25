package br.com.alr.api.sbkafkaproducersample.application.usecase;

import br.com.alr.api.sbkafkaproducersample.application.exception.BusinessValidationException;
import br.com.alr.api.sbkafkaproducersample.application.port.out.InvoiceEmailPort;
import br.com.alr.api.sbkafkaproducersample.application.port.out.OutboxEventPort;
import br.com.alr.api.sbkafkaproducersample.domain.enumtype.OutboxStatus;
import br.com.alr.api.sbkafkaproducersample.domain.model.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
public class CreateInvoiceEmailFromOrderCreatedEventService {

  private final LoadOrderUserService loadOrderUserService;
  private final InvoiceEmailBodyFactory invoiceEmailBodyFactory;
  private final InvoiceEmailPort invoiceEmailPort;
  private final OutboxEventPort outboxEventPort;
  private final ObjectMapper objectMapper;

  public CreateInvoiceEmailFromOrderCreatedEventService(
      LoadOrderUserService loadOrderUserService,
      InvoiceEmailBodyFactory invoiceEmailBodyFactory,
      InvoiceEmailPort invoiceEmailPort,
      OutboxEventPort outboxEventPort,
      ObjectMapper objectMapper
  ) {
    this.loadOrderUserService = loadOrderUserService;
    this.invoiceEmailBodyFactory = invoiceEmailBodyFactory;
    this.invoiceEmailPort = invoiceEmailPort;
    this.outboxEventPort = outboxEventPort;
    this.objectMapper = objectMapper;
  }

  @Transactional
  public void create(OrderCreatedEvent event) {
    User user = loadOrderUserService.loadById(event.userId());
    InvoiceEmail invoiceEmail = invoiceEmailPort.save(new InvoiceEmail(
        UUID.randomUUID(),
        user.id(),
        event.orderId(),
        user.email(),
        "Invoice for order #" + event.orderId(),
        invoiceEmailBodyFactory.create(user, event),
        OffsetDateTime.now(),
        null
    ));

    outboxEventPort.save(new OutboxEvent(
        UUID.randomUUID(),
        OutboxAggregateType.INVOICE_EMAIL,
        invoiceEmail.id().toString(),
        OutboxEventType.EMAIL_INVOICE_REQUESTED,
        createPayload(new InvoiceEmailOutboxPayload(invoiceEmail.id())),
        OutboxStatus.PENDING,
        OffsetDateTime.now(),
        null,
        null
    ));
  }

  private String createPayload(InvoiceEmailOutboxPayload payload) {
    try {
      return objectMapper.writeValueAsString(payload);
    } catch (JacksonException exception) {
      throw new BusinessValidationException("Could not serialize invoice email outbox payload");
    }
  }
}
