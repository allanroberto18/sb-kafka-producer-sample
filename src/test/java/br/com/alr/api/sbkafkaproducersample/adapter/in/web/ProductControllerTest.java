package br.com.alr.api.sbkafkaproducersample.adapter.in.web;

import br.com.alr.api.sbkafkaproducersample.application.port.in.ListProductsUseCase;
import br.com.alr.api.sbkafkaproducersample.domain.model.Product;
import br.com.alr.api.sbkafkaproducersample.support.TestMappers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ProductControllerTest {

  private MockMvc mockMvc;
  private ListProductsUseCase listProductsUseCase;

  @BeforeEach
  void setUp() {
    listProductsUseCase = mock(ListProductsUseCase.class);
    mockMvc = MockMvcBuilders
        .standaloneSetup(new ProductController(listProductsUseCase, TestMappers.apiMapper()))
        .setControllerAdvice(new RestControllerExceptionHandler())
        .build();
  }

  @Test
  void shouldListProducts() throws Exception {
    when(listProductsUseCase.findAll()).thenReturn(List.of(new Product(1L, "Notebook", new BigDecimal("12.50"))));

    mockMvc.perform(get("/api/products"))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$[0].id").value(1))
        .andExpect(jsonPath("$[0].name").value("Notebook"));
  }
}
