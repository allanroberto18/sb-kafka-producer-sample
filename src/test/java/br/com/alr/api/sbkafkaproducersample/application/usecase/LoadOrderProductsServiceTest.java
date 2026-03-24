package br.com.alr.api.sbkafkaproducersample.application.usecase;

import br.com.alr.api.sbkafkaproducersample.application.dto.CreateOrderItemCommand;
import br.com.alr.api.sbkafkaproducersample.application.exception.NotFoundException;
import br.com.alr.api.sbkafkaproducersample.application.port.out.ProductQueryPort;
import br.com.alr.api.sbkafkaproducersample.domain.model.Product;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoadOrderProductsServiceTest {

  @Mock
  private ProductQueryPort productQueryPort;

  @Test
  void shouldReturnProductsMappedById() {
    LoadOrderProductsService service = new LoadOrderProductsService(productQueryPort);
    List<CreateOrderItemCommand> items = List.of(
        new CreateOrderItemCommand(10L, 2),
        new CreateOrderItemCommand(20L, 1)
    );

    when(productQueryPort.findAllByIds(Set.of(10L, 20L))).thenReturn(List.of(
        new Product(10L, "Notebook", new BigDecimal("12.50")),
        new Product(20L, "Keyboard", new BigDecimal("45.90"))
    ));

    Map<Long, Product> products = service.loadProductsByItems(items);

    assertThat(products).hasSize(2);
    assertThat(products.get(10L).name()).isEqualTo("Notebook");
    assertThat(products.get(20L).price()).isEqualByComparingTo("45.90");
  }

  @Test
  void shouldFailWhenAnyProductIsMissing() {
    LoadOrderProductsService service = new LoadOrderProductsService(productQueryPort);
    List<CreateOrderItemCommand> items = List.of(
        new CreateOrderItemCommand(10L, 2),
        new CreateOrderItemCommand(20L, 1)
    );

    when(productQueryPort.findAllByIds(Set.of(10L, 20L))).thenReturn(List.of(
        new Product(10L, "Notebook", new BigDecimal("12.50"))
    ));

    assertThatThrownBy(() -> service.loadProductsByItems(items))
        .isInstanceOf(NotFoundException.class)
        .hasMessageContaining("products were not found");
  }
}
