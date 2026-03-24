package br.com.alr.api.sbkafkaproducersample.adapter.out.persistence.repository;

import br.com.alr.api.sbkafkaproducersample.adapter.out.persistence.entity.ProductJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductJpaRepository extends JpaRepository<ProductJpaEntity, Long> {

  List<ProductJpaEntity> findByIdIn(Iterable<Long> ids);
}
