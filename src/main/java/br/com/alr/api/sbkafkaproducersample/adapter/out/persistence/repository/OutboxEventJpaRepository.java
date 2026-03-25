package br.com.alr.api.sbkafkaproducersample.adapter.out.persistence.repository;

import br.com.alr.api.sbkafkaproducersample.adapter.out.persistence.entity.OutboxEventJpaEntity;
import br.com.alr.api.sbkafkaproducersample.domain.enumtype.OutboxStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface OutboxEventJpaRepository extends JpaRepository<OutboxEventJpaEntity, UUID> {

  List<OutboxEventJpaEntity> findAllByStatusInAndAttemptCountLessThanOrderByCreatedAtAsc(
      Collection<OutboxStatus> status,
      Integer maxAttempts,
      Pageable pageable
  );
}
