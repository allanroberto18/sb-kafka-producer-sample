package br.com.alr.api.sbkafkaproducersample.adapter.out.messaging;

import br.com.alr.api.sbkafkaproducersample.adapter.out.persistence.entity.ProductJpaEntity;
import br.com.alr.api.sbkafkaproducersample.adapter.out.persistence.entity.UserJpaEntity;
import br.com.alr.api.sbkafkaproducersample.adapter.out.persistence.repository.InvoiceEmailJpaRepository;
import br.com.alr.api.sbkafkaproducersample.adapter.out.persistence.repository.ProductJpaRepository;
import br.com.alr.api.sbkafkaproducersample.adapter.out.persistence.repository.UserJpaRepository;
import br.com.alr.api.sbkafkaproducersample.application.port.out.EmailSenderPort;
import br.com.alr.api.sbkafkaproducersample.application.port.out.InvoiceEmailPort;
import br.com.alr.api.sbkafkaproducersample.application.port.out.OrderPersistencePort;
import br.com.alr.api.sbkafkaproducersample.application.port.out.OutboxEventPort;
import br.com.alr.api.sbkafkaproducersample.domain.enumtype.OrderStatus;
import br.com.alr.api.sbkafkaproducersample.domain.enumtype.OutboxStatus;
import br.com.alr.api.sbkafkaproducersample.domain.model.*;
import br.com.alr.api.sbkafkaproducersample.support.PostgresContainerIT;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
    "app.outbox.publisher.enabled=true",
    "app.outbox.fixed-delay-ms=3600000",
    "app.kafka.order-topic=orders.created.email-dispatcher.test"
})
class EmailInvoiceOutboxDispatcherIntegrationTest extends PostgresContainerIT {

  @Autowired
  private UserJpaRepository userJpaRepository;
  @Autowired
  private ProductJpaRepository productJpaRepository;
  @Autowired
  private OrderPersistencePort orderPersistencePort;
  @Autowired
  private InvoiceEmailPort invoiceEmailPort;
  @Autowired
  private InvoiceEmailJpaRepository invoiceEmailJpaRepository;
  @Autowired
  private OutboxEventPort outboxEventPort;
  @Autowired
  private OutboxPublisher outboxPublisher;
  @Autowired
  private ObjectMapper objectMapper;
  @Autowired
  private TestEmailSender testEmailSender;

  @Test
  void shouldDispatchInvoiceEmailAndUpdateOrderAndOutboxStatus() throws Exception {
    UserJpaEntity user = userJpaRepository.findAll().getFirst();
    ProductJpaEntity product = productJpaRepository.findAll().getFirst();
    Order order = orderPersistencePort.save(new Order(
        null,
        user.getId(),
        user.getName(),
        OrderStatus.CREATED,
        new BigDecimal("25.00"),
        OffsetDateTime.now(),
        List.of(new OrderItem(null, product.getId(), product.getName(), 2, product.getPrice(), new BigDecimal("25.00")))
    ));
    InvoiceEmail invoiceEmail = invoiceEmailPort.save(new InvoiceEmail(
        UUID.randomUUID(),
        user.getId(),
        order.id(),
        user.getEmail(),
        "Invoice for order #" + order.id(),
        "Hello " + user.getName(),
        OffsetDateTime.now(),
        null
    ));
    UUID outboxEventId = UUID.randomUUID();

    outboxEventPort.save(new OutboxEvent(
        outboxEventId,
        OutboxAggregateType.INVOICE_EMAIL,
        invoiceEmail.id().toString(),
        OutboxEventType.EMAIL_INVOICE_REQUESTED,
        objectMapper.writeValueAsString(new InvoiceEmailOutboxPayload(invoiceEmail.id())),
        OutboxStatus.PENDING,
        OffsetDateTime.now(),
        null,
        null
    ));

    outboxPublisher.publishPendingEvents();

    assertThat(testEmailSender.sentEmails)
        .extracting(InvoiceEmail::id)
        .contains(invoiceEmail.id());

    assertThat(invoiceEmailJpaRepository.findById(invoiceEmail.id()))
        .get()
        .extracting(invoiceEmailJpaEntity -> invoiceEmailJpaEntity.getSentAt())
        .isNotNull();

    assertThat(orderPersistencePort.findById(order.id()))
        .get()
        .extracting(Order::status)
        .isEqualTo(OrderStatus.INVOICE_DELIVERED);

    assertThat(outboxEventPort.findAll())
        .filteredOn(event -> event.id().equals(outboxEventId))
        .singleElement()
        .extracting(OutboxEvent::status)
        .isEqualTo(OutboxStatus.PUBLISHED);
  }

  @TestConfiguration
  static class EmailTestConfiguration {

    @Bean
    TestEmailSender testEmailSender() {
      return new TestEmailSender();
    }

    @Bean
    @Primary
    EmailSenderPort emailSenderPort(TestEmailSender testEmailSender) {
      return testEmailSender;
    }
  }

  static class TestEmailSender implements EmailSenderPort {
    private final List<InvoiceEmail> sentEmails = new ArrayList<>();

    @Override
    public void send(InvoiceEmail invoiceEmail) {
      sentEmails.add(invoiceEmail);
    }
  }
}
