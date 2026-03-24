package br.com.alr.api.sbkafkaproducersample.adapter.in.web;

import br.com.alr.api.sbkafkaproducersample.adapter.in.web.request.CreateOrderRequest;
import br.com.alr.api.sbkafkaproducersample.adapter.in.web.response.OrderResponse;
import br.com.alr.api.sbkafkaproducersample.application.port.in.CreateOrderUseCase;
import br.com.alr.api.sbkafkaproducersample.application.port.in.ListOrdersUseCase;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

  private final CreateOrderUseCase createOrderUseCase;
  private final ListOrdersUseCase listOrdersUseCase;
  private final ApiMapper apiMapper;

  public OrderController(
      CreateOrderUseCase createOrderUseCase,
      ListOrdersUseCase listOrdersUseCase,
      ApiMapper apiMapper
  ) {
    this.createOrderUseCase = createOrderUseCase;
    this.listOrdersUseCase = listOrdersUseCase;
    this.apiMapper = apiMapper;
  }

  @Operation(summary = "List orders")
  @GetMapping
  public List<OrderResponse> findAll() {
    return listOrdersUseCase.findAll().stream().map(apiMapper::toResponse).toList();
  }

  @Operation(summary = "Create order asynchronously")
  @PostMapping
  public CompletableFuture<ResponseEntity<OrderResponse>> create(@Valid @RequestBody CreateOrderRequest request) {
    return createOrderUseCase.create(apiMapper.toCommand(request))
        .thenApply(order -> ResponseEntity.status(HttpStatus.ACCEPTED).body(apiMapper.toResponse(order)));
  }
}
