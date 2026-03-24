package br.com.alr.api.sbkafkaproducersample.application.usecase;

import br.com.alr.api.sbkafkaproducersample.application.port.out.ProductQueryPort;
import br.com.alr.api.sbkafkaproducersample.application.port.out.UserQueryPort;
import br.com.alr.api.sbkafkaproducersample.domain.model.Product;
import br.com.alr.api.sbkafkaproducersample.domain.model.User;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ListCatalogServicesTest {

  @Test
  void shouldListUsers() {
    UserQueryPort port = mock(UserQueryPort.class);
    when(port.findAll()).thenReturn(List.of(new User(1L, "User", "user@example.com")));

    assertThat(new ListUsersService(port).findAll()).hasSize(1);
  }

  @Test
  void shouldListProducts() {
    ProductQueryPort port = mock(ProductQueryPort.class);
    when(port.findAll()).thenReturn(List.of(new Product(1L, "Notebook", new BigDecimal("12.50"))));

    assertThat(new ListProductsService(port).findAll()).hasSize(1);
  }
}
