package br.com.alr.api.sbkafkaproducersample.adapter.out.persistence;

import br.com.alr.api.sbkafkaproducersample.domain.enumtype.OutboxStatus;
import br.com.alr.api.sbkafkaproducersample.domain.model.OutboxEvent;
import br.com.alr.api.sbkafkaproducersample.support.PostgresContainerIT;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class OutboxEventPersistenceAdapterTest extends PostgresContainerIT {

  @Autowired
  private OutboxEventPersistenceAdapter outboxEventPersistenceAdapter;

  @Test
  void shouldPersistAndTransitionOutboxEvents() {
    UUID id = UUID.fromString("11111111-1111-1111-1111-111111111111");
    outboxEventPersistenceAdapter.save(new OutboxEvent(
        id,
        "ORDER",
        "1",
        "ORDER_CREATED",
        "{\"orderId\":1}",
        OutboxStatus.PENDING,
        OffsetDateTime.parse("2026-03-24T10:15:30Z"),
        null,
        null
    ));

    assertThat(outboxEventPersistenceAdapter.findProcessableEvents(10)).hasSize(1);

    outboxEventPersistenceAdapter.markPublished(id);

    assertThat(outboxEventPersistenceAdapter.findAll())
        .singleElement()
        .extracting(OutboxEvent::status)
        .isEqualTo(OutboxStatus.PUBLISHED);
  }
}
