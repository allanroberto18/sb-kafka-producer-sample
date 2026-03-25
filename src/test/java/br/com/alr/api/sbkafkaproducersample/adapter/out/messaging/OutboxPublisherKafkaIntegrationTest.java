package br.com.alr.api.sbkafkaproducersample.adapter.out.messaging;

import br.com.alr.api.sbkafkaproducersample.application.port.out.OutboxEventPort;
import br.com.alr.api.sbkafkaproducersample.domain.enumtype.OrderStatus;
import br.com.alr.api.sbkafkaproducersample.domain.enumtype.OutboxStatus;
import br.com.alr.api.sbkafkaproducersample.domain.model.OrderCreatedEvent;
import br.com.alr.api.sbkafkaproducersample.domain.model.OrderCreatedItemEvent;
import br.com.alr.api.sbkafkaproducersample.domain.model.OutboxEvent;
import br.com.alr.api.sbkafkaproducersample.support.PostgresContainerIT;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
    "app.outbox.publisher.enabled=true",
    "app.outbox.fixed-delay-ms=3600000",
    "app.kafka.order-topic=orders.created.outbox-publisher.test"
})
class OutboxPublisherKafkaIntegrationTest extends PostgresContainerIT {

  @Autowired
  private OutboxEventPort outboxEventPort;
  @Autowired
  private OutboxPublisher outboxPublisher;
  @Autowired
  private ObjectMapper objectMapper;
  @Value("${spring.kafka.bootstrap-servers}")
  private String bootstrapServers;
  @Value("${app.kafka.order-topic}")
  private String topic;

  @Test
  void shouldPublishPendingOutboxEventToKafka() throws Exception {
    OrderCreatedEvent payload = new OrderCreatedEvent(
        99L,
        1L,
        "Default User",
        OrderStatus.CREATED,
        new BigDecimal("70.90"),
        OffsetDateTime.parse("2026-03-24T12:00:00Z"),
        List.of(new OrderCreatedItemEvent(
            10L,
            "Notebook",
            2,
            new BigDecimal("12.50"),
            new BigDecimal("25.00")
        ))
    );

    UUID eventId = UUID.randomUUID();
    outboxEventPort.save(new OutboxEvent(
        eventId,
        "ORDER",
        "99",
        "ORDER_CREATED",
        objectMapper.writeValueAsString(payload),
        OutboxStatus.PENDING,
        OffsetDateTime.now(),
        null,
        null
    ));

    try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(consumerProperties())) {
      consumer.subscribe(List.of(topic));

      outboxPublisher.publishPendingEvents();

      ConsumerRecord<String, String> publishedRecord = pollSingleRecord(consumer);
      assertThat(publishedRecord.key()).isEqualTo("99");

      OrderCreatedEvent publishedEvent = objectMapper.readValue(publishedRecord.value(), OrderCreatedEvent.class);
      assertThat(publishedEvent.orderId()).isEqualTo(99L);
      assertThat(publishedEvent.userId()).isEqualTo(1L);
      assertThat(publishedEvent.status()).isEqualTo(OrderStatus.CREATED);

      assertThat(outboxEventPort.findAll())
          .filteredOn(event -> event.id().equals(eventId))
          .singleElement()
          .extracting(OutboxEvent::status)
          .isEqualTo(OutboxStatus.PUBLISHED);
    }
  }

  private Properties consumerProperties() {
    Properties properties = new Properties();
    properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    properties.put(ConsumerConfig.GROUP_ID_CONFIG, "outbox-publisher-test-" + UUID.randomUUID());
    properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
    properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
    properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
    return properties;
  }

  private ConsumerRecord<String, String> pollSingleRecord(KafkaConsumer<String, String> consumer) {
    long deadline = System.currentTimeMillis() + Duration.ofSeconds(10).toMillis();
    while (System.currentTimeMillis() < deadline) {
      ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(500));
      for (ConsumerRecord<String, String> record : records) {
        if (topic.equals(record.topic())) {
          return record;
        }
      }
    }
    throw new AssertionError("No Kafka record was published to topic " + topic);
  }
}
