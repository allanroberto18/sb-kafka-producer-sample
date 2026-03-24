package br.com.alr.api.sbkafkaproducersample.adapter.in.web;

import br.com.alr.api.sbkafkaproducersample.adapter.in.web.response.ProductResponse;
import br.com.alr.api.sbkafkaproducersample.application.port.in.ListProductsUseCase;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

  private final ListProductsUseCase listProductsUseCase;
  private final ApiMapper apiMapper;

  public ProductController(ListProductsUseCase listProductsUseCase, ApiMapper apiMapper) {
    this.listProductsUseCase = listProductsUseCase;
    this.apiMapper = apiMapper;
  }

  @Operation(summary = "List products")
  @GetMapping
  public List<ProductResponse> findAll() {
    return listProductsUseCase.findAll().stream().map(apiMapper::toResponse).toList();
  }
}
