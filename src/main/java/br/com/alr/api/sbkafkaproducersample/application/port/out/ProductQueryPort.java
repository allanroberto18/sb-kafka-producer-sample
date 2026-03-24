package br.com.alr.api.sbkafkaproducersample.application.port.out;

import br.com.alr.api.sbkafkaproducersample.domain.model.Product;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface ProductQueryPort {

  List<Product> findAll();

  Optional<Product> findById(Long id);

  List<Product> findAllByIds(Set<Long> ids);
}
