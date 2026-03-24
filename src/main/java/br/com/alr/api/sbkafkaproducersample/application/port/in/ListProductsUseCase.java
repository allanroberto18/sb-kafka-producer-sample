package br.com.alr.api.sbkafkaproducersample.application.port.in;

import br.com.alr.api.sbkafkaproducersample.domain.model.Product;

import java.util.List;

public interface ListProductsUseCase {

  List<Product> findAll();
}
