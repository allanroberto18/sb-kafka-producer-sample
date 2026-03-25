package br.com.alr.api.sbkafkaproducersample.adapter.out.persistence.repository;

import br.com.alr.api.sbkafkaproducersample.adapter.out.persistence.entity.OrderJpaEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderJpaRepository extends JpaRepository<OrderJpaEntity, Long> {

  @Override
  @EntityGraph(attributePaths = {"user", "items", "items.product"})
  List<OrderJpaEntity> findAll();

  @Override
  @EntityGraph(attributePaths = {"user", "items", "items.product"})
  Optional<OrderJpaEntity> findById(Long id);
}
