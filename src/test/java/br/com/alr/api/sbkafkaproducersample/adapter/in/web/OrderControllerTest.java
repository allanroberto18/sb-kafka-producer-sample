package br.com.alr.api.sbkafkaproducersample.adapter.in.web;

import br.com.alr.api.sbkafkaproducersample.application.exception.NotFoundException;
import br.com.alr.api.sbkafkaproducersample.application.port.in.CreateOrderUseCase;
import br.com.alr.api.sbkafkaproducersample.application.port.in.ListOrdersUseCase;
import br.com.alr.api.sbkafkaproducersample.domain.enumtype.OrderStatus;
import br.com.alr.api.sbkafkaproducersample.domain.model.Order;
import br.com.alr.api.sbkafkaproducersample.domain.model.OrderItem;
import br.com.alr.api.sbkafkaproducersample.support.TestMappers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class OrderControllerTest {

  private MockMvc mockMvc;
  private CreateOrderUseCase createOrderUseCase;
  private ListOrdersUseCase listOrdersUseCase;

  @BeforeEach
  void setUp() {
    createOrderUseCase = mock(CreateOrderUseCase.class);
    listOrdersUseCase = mock(ListOrdersUseCase.class);
    mockMvc = MockMvcBuilders
        .standaloneSetup(new OrderController(createOrderUseCase, listOrdersUseCase, TestMappers.apiMapper()))
        .setControllerAdvice(new RestControllerExceptionHandler())
        .build();
  }

  @Test
  void shouldCreateOrderAsync() throws Exception {
    when(createOrderUseCase.create(any())).thenReturn(CompletableFuture.completedFuture(sampleOrder()));

    var mvcResult = mockMvc.perform(post("/api/orders")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "userId": 1,
                  "items": [
                    {
                      "productId": 10,
                      "quantity": 2
                    }
                  ]
                }
                """))
        .andExpect(request().asyncStarted())
        .andReturn();

    mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch(mvcResult))
        .andExpect(status().isAccepted())
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.items[0].productId").value(10));
  }

  @Test
  void shouldValidatePayload() throws Exception {
    mockMvc.perform(post("/api/orders")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "items": []
                }
                """))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.title").value("Bad Request"))
        .andExpect(jsonPath("$.errors").isArray());
  }

  @Test
  void shouldReturnNotFoundProblem() throws Exception {
    when(createOrderUseCase.create(any())).thenReturn(CompletableFuture.failedFuture(new NotFoundException("User not found")));

    var mvcResult = mockMvc.perform(post("/api/orders")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "userId": 99,
                  "items": [
                    {
                      "productId": 10,
                      "quantity": 1
                    }
                  ]
                }
                """))
        .andExpect(request().asyncStarted())
        .andReturn();

    mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch(mvcResult))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.title").value("Resource Not Found"));
  }

  @Test
  void shouldListOrders() throws Exception {
    when(listOrdersUseCase.findAll()).thenReturn(List.of(sampleOrder()));

    mockMvc.perform(get("/api/orders"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].userName").value("Default User"));
  }

  private Order sampleOrder() {
    return new Order(
        1L,
        1L,
        "Default User",
        OrderStatus.CREATED,
        new BigDecimal("25.00"),
        OffsetDateTime.parse("2026-03-24T10:15:30Z"),
        List.of(new OrderItem(1L, 10L, "Notebook", 2, new BigDecimal("12.50"), new BigDecimal("25.00")))
    );
  }
}
