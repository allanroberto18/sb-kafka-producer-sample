package br.com.alr.api.sbkafkaproducersample.adapter.in.messaging;

import br.com.alr.api.sbkafkaproducersample.adapter.out.persistence.entity.ProductJpaEntity;
import br.com.alr.api.sbkafkaproducersample.adapter.out.persistence.entity.UserJpaEntity;
import br.com.alr.api.sbkafkaproducersample.adapter.out.persistence.repository.InvoiceEmailJpaRepository;
import br.com.alr.api.sbkafkaproducersample.adapter.out.persistence.repository.ProductJpaRepository;
import br.com.alr.api.sbkafkaproducersample.adapter.out.persistence.repository.UserJpaRepository;
import br.com.alr.api.sbkafkaproducersample.application.port.out.OrderPersistencePort;
import br.com.alr.api.sbkafkaproducersample.application.port.out.OutboxEventPort;
import br.com.alr.api.sbkafkaproducersample.domain.enumtype.OrderStatus;
import br.com.alr.api.sbkafkaproducersample.domain.enumtype.OutboxStatus;
import br.com.alr.api.sbkafkaproducersample.domain.model.*;
import br.com.alr.api.sbkafkaproducersample.support.PostgresContainerIT;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
    "app.outbox.publisher.enabled=false",
    "app.kafka.invoice-consumer-enabled=true",
    "app.kafka.order-topic=orders.created.invoice-consumer.test"
})
class OrderCreatedInvoiceConsumerIntegrationTest extends PostgresContainerIT {

  @Autowired
  private UserJpaRepository userJpaRepository;
  @Autowired
  private ProductJpaRepository productJpaRepository;
  @Autowired
  private OrderPersistencePort orderPersistencePort;
  @Autowired
  private InvoiceEmailJpaRepository invoiceEmailJpaRepository;
  @Autowired
  private OutboxEventPort outboxEventPort;
  @Autowired
  private ObjectMapper objectMapper;

  @Value("${spring.kafka.bootstrap-servers}")
  private String bootstrapServers;
  @Value("${app.kafka.order-topic}")
  private String topic;

  @Test
  void shouldConsumeOrderCreatedEventAndCreateInvoiceEmailAndOutbox() throws Exception {
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

    OrderCreatedEvent event = new OrderCreatedEvent(
        order.id(),
        user.getId(),
        order.userName(),
        order.status(),
        order.totalAmount(),
        order.createdAt(),
        List.of(new OrderCreatedItemEvent(product.getId(), product.getName(), 2, product.getPrice(), new BigDecimal("25.00")))
    );

    try (KafkaProducer<String, String> producer = new KafkaProducer<>(producerProperties())) {
      producer.send(new ProducerRecord<>(topic, order.id().toString(), objectMapper.writeValueAsString(event))).get();
    }

    awaitUntil(() -> !invoiceEmailJpaRepository.findAll().isEmpty());

    assertThat(invoiceEmailJpaRepository.findAll())
        .singleElement()
        .satisfies(invoiceEmail -> {
          assertThat(invoiceEmail.getUser().getId()).isEqualTo(user.getId());
          assertThat(invoiceEmail.getOrder().getId()).isEqualTo(order.id());
          assertThat(invoiceEmail.getRecipientEmail()).isEqualTo(user.getEmail());
          assertThat(invoiceEmail.getBody()).contains(user.getName());
        });

    assertThat(outboxEventPort.findAll())
        .filteredOn(outboxEvent -> OutboxEventType.EMAIL_INVOICE_REQUESTED.equals(outboxEvent.eventType()))
        .singleElement()
        .extracting(OutboxEvent::status)
        .isEqualTo(OutboxStatus.PENDING);
  }

  private Properties producerProperties() {
    Properties properties = new Properties();
    properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
    properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
    return properties;
  }

  private void awaitUntil(Check check) throws Exception {
    long deadline = System.currentTimeMillis() + Duration.ofSeconds(10).toMillis();
    while (System.currentTimeMillis() < deadline) {
      if (check.isComplete()) {
        return;
      }
      Thread.sleep(200);
    }
    throw new AssertionError("Condition was not satisfied within the timeout");
  }

  @FunctionalInterface
  private interface Check {
    boolean isComplete() throws Exception;
  }
}
