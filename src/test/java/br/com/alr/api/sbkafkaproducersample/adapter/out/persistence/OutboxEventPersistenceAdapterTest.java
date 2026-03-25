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

    assertThat(outboxEventPersistenceAdapter.findProcessableEvents(10, 5))
        .filteredOn(event -> event.id().equals(id))
        .singleElement()
        .extracting(OutboxEvent::status)
        .isEqualTo(OutboxStatus.PENDING);

    outboxEventPersistenceAdapter.markPublished(id);

    assertThat(outboxEventPersistenceAdapter.findAll())
        .filteredOn(event -> event.id().equals(id))
        .singleElement()
        .extracting(OutboxEvent::status)
        .isEqualTo(OutboxStatus.PUBLISHED);
  }

  @Test
  void shouldStopReturningFailedEventsAfterMaxAttempts() {
    UUID id = UUID.fromString("33333333-3333-3333-3333-333333333333");
    outboxEventPersistenceAdapter.save(new OutboxEvent(
        id,
        "ORDER",
        "2",
        "ORDER_CREATED",
        "{\"orderId\":2}",
        OutboxStatus.PENDING,
        OffsetDateTime.parse("2026-03-24T10:15:30Z"),
        null,
        null
    ));

    outboxEventPersistenceAdapter.markFailed(id, "first");
    outboxEventPersistenceAdapter.markFailed(id, "second");

    assertThat(outboxEventPersistenceAdapter.findProcessableEvents(10, 3))
        .extracting(OutboxEvent::id)
        .contains(id);
    assertThat(outboxEventPersistenceAdapter.findProcessableEvents(10, 2))
        .extracting(OutboxEvent::id)
        .doesNotContain(id);
  }
}
