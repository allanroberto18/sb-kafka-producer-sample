package br.com.alr.api.sbkafkaproducersample.adapter.out.persistence;

import br.com.alr.api.sbkafkaproducersample.adapter.out.persistence.entity.OutboxEventJpaEntity;
import br.com.alr.api.sbkafkaproducersample.adapter.out.persistence.mapper.PersistenceMapper;
import br.com.alr.api.sbkafkaproducersample.adapter.out.persistence.repository.OutboxEventJpaRepository;
import br.com.alr.api.sbkafkaproducersample.application.port.out.OutboxEventPort;
import br.com.alr.api.sbkafkaproducersample.domain.enumtype.OutboxStatus;
import br.com.alr.api.sbkafkaproducersample.domain.model.OutboxEvent;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Component
public class OutboxEventPersistenceAdapter implements OutboxEventPort {

  private final OutboxEventJpaRepository outboxEventJpaRepository;
  private final PersistenceMapper persistenceMapper;

  public OutboxEventPersistenceAdapter(
      OutboxEventJpaRepository outboxEventJpaRepository,
      PersistenceMapper persistenceMapper
  ) {
    this.outboxEventJpaRepository = outboxEventJpaRepository;
    this.persistenceMapper = persistenceMapper;
  }

  @Override
  public OutboxEvent save(OutboxEvent event) {
    return persistenceMapper.toDomain(outboxEventJpaRepository.save(persistenceMapper.toEntity(event)));
  }

  @Override
  public List<OutboxEvent> findAll() {
    return outboxEventJpaRepository.findAll().stream().map(persistenceMapper::toDomain).toList();
  }

  @Override
  public List<OutboxEvent> findProcessableEvents(int limit, int maxAttempts) {
    return outboxEventJpaRepository
        .findAllByStatusInAndAttemptCountLessThanOrderByCreatedAtAsc(
            List.of(OutboxStatus.PENDING, OutboxStatus.FAILED),
            maxAttempts,
            PageRequest.of(0, limit)
        )
        .stream()
        .map(persistenceMapper::toDomain)
        .toList();
  }

  @Transactional
  @Override
  public void markPublished(UUID eventId) {
    OutboxEventJpaEntity entity = outboxEventJpaRepository.getReferenceById(eventId);
    entity.setStatus(OutboxStatus.PUBLISHED);
    entity.setProcessedAt(OffsetDateTime.now());
    entity.setErrorMessage(null);
  }

  @Transactional
  @Override
  public void markFailed(UUID eventId, String errorMessage) {
    OutboxEventJpaEntity entity = outboxEventJpaRepository.getReferenceById(eventId);
    entity.setStatus(OutboxStatus.FAILED);
    entity.setProcessedAt(OffsetDateTime.now());
    entity.setErrorMessage(errorMessage);
    entity.setAttemptCount(entity.getAttemptCount() + 1);
  }
}
