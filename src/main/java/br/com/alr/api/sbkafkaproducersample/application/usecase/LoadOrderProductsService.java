package br.com.alr.api.sbkafkaproducersample.application.usecase;

import br.com.alr.api.sbkafkaproducersample.application.dto.CreateOrderItemCommand;
import br.com.alr.api.sbkafkaproducersample.application.exception.NotFoundException;
import br.com.alr.api.sbkafkaproducersample.application.port.out.ProductQueryPort;
import br.com.alr.api.sbkafkaproducersample.domain.model.Product;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class LoadOrderProductsService {

  private final ProductQueryPort productQueryPort;

  public LoadOrderProductsService(ProductQueryPort productQueryPort) {
    this.productQueryPort = productQueryPort;
  }

  public Map<Long, Product> loadProductsByItems(List<CreateOrderItemCommand> items) {
    Set<Long> productIds = items.stream()
        .map(CreateOrderItemCommand::productId)
        .collect(Collectors.toSet());

    Map<Long, Product> productMap = productQueryPort.findAllByIds(productIds).stream()
        .collect(Collectors.toMap(Product::id, product -> product));

    if (productMap.size() != productIds.size()) {
      throw new NotFoundException("One or more products were not found");
    }

    return productMap;
  }
}
