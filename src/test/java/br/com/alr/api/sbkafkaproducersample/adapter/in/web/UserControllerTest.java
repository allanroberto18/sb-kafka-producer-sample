package br.com.alr.api.sbkafkaproducersample.adapter.in.web;

import br.com.alr.api.sbkafkaproducersample.application.port.in.ListUsersUseCase;
import br.com.alr.api.sbkafkaproducersample.domain.model.User;
import br.com.alr.api.sbkafkaproducersample.support.TestMappers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class UserControllerTest {

  private MockMvc mockMvc;
  private ListUsersUseCase listUsersUseCase;

  @BeforeEach
  void setUp() {
    listUsersUseCase = mock(ListUsersUseCase.class);
    mockMvc = MockMvcBuilders
        .standaloneSetup(new UserController(listUsersUseCase, TestMappers.apiMapper()))
        .setControllerAdvice(new RestControllerExceptionHandler())
        .build();
  }

  @Test
  void shouldListUsers() throws Exception {
    when(listUsersUseCase.findAll()).thenReturn(List.of(new User(1L, "Default User", "default.user@example.com")));

    mockMvc.perform(get("/api/users"))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$[0].id").value(1))
        .andExpect(jsonPath("$[0].name").value("Default User"));
  }
}
