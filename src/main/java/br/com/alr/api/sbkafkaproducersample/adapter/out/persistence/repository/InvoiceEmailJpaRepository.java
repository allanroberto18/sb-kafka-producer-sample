package br.com.alr.api.sbkafkaproducersample.adapter.out.persistence.repository;

import br.com.alr.api.sbkafkaproducersample.adapter.out.persistence.entity.InvoiceEmailJpaEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface InvoiceEmailJpaRepository extends JpaRepository<InvoiceEmailJpaEntity, UUID> {

  @Override
  @EntityGraph(attributePaths = {"user", "order"})
  Optional<InvoiceEmailJpaEntity> findById(UUID id);
}
