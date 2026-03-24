package br.com.alr.api.sbkafkaproducersample.adapter.in.web;

import br.com.alr.api.sbkafkaproducersample.adapter.in.web.request.CreateOrderRequest;
import br.com.alr.api.sbkafkaproducersample.adapter.in.web.response.*;
import br.com.alr.api.sbkafkaproducersample.application.dto.CreateOrderCommand;
import br.com.alr.api.sbkafkaproducersample.domain.model.*;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
public class ApiMapper {

  private final ModelMapper modelMapper;

  public ApiMapper(ModelMapper modelMapper) {
    this.modelMapper = modelMapper;
  }

  public CreateOrderCommand toCommand(CreateOrderRequest request) {
    return modelMapper.map(request, CreateOrderCommand.class);
  }

  public UserResponse toResponse(User user) {
    return modelMapper.map(user, UserResponse.class);
  }

  public ProductResponse toResponse(Product product) {
    return modelMapper.map(product, ProductResponse.class);
  }

  public OrderResponse toResponse(Order order) {
    return modelMapper.map(order, OrderResponse.class);
  }

  public OrderItemResponse toResponse(OrderItem item) {
    return modelMapper.map(item, OrderItemResponse.class);
  }

  public OutboxEventResponse toResponse(OutboxEvent event) {
    return modelMapper.map(event, OutboxEventResponse.class);
  }
}
