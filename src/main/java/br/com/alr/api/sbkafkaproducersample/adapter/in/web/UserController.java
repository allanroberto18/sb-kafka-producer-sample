package br.com.alr.api.sbkafkaproducersample.adapter.in.web;

import br.com.alr.api.sbkafkaproducersample.adapter.in.web.response.UserResponse;
import br.com.alr.api.sbkafkaproducersample.application.port.in.ListUsersUseCase;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

  private final ListUsersUseCase listUsersUseCase;
  private final ApiMapper apiMapper;

  public UserController(ListUsersUseCase listUsersUseCase, ApiMapper apiMapper) {
    this.listUsersUseCase = listUsersUseCase;
    this.apiMapper = apiMapper;
  }

  @Operation(summary = "List users")
  @GetMapping
  public List<UserResponse> findAll() {
    return listUsersUseCase.findAll().stream().map(apiMapper::toResponse).toList();
  }
}
