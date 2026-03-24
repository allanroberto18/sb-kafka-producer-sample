package br.com.alr.api.sbkafkaproducersample.adapter.out.persistence;

import br.com.alr.api.sbkafkaproducersample.adapter.out.persistence.entity.ProductJpaEntity;
import br.com.alr.api.sbkafkaproducersample.adapter.out.persistence.entity.UserJpaEntity;
import br.com.alr.api.sbkafkaproducersample.adapter.out.persistence.repository.ProductJpaRepository;
import br.com.alr.api.sbkafkaproducersample.adapter.out.persistence.repository.UserJpaRepository;
import br.com.alr.api.sbkafkaproducersample.domain.enumtype.OrderStatus;
import br.com.alr.api.sbkafkaproducersample.domain.model.Order;
import br.com.alr.api.sbkafkaproducersample.domain.model.OrderItem;
import br.com.alr.api.sbkafkaproducersample.support.PostgresContainerIT;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class OrderPersistenceAdapterTest extends PostgresContainerIT {

  @Autowired
  private OrderPersistenceAdapter orderPersistenceAdapter;
  @Autowired
  private UserJpaRepository userJpaRepository;
  @Autowired
  private ProductJpaRepository productJpaRepository;

  @BeforeEach
  void setUp() {
    productJpaRepository.deleteAll();
    userJpaRepository.deleteAll();
  }

  @Test
  void shouldPersistOrderAndItems() {
    UserJpaEntity user = userJpaRepository.save(UserJpaEntity.builder().name("User").email("user@example.com").build());
    ProductJpaEntity product = productJpaRepository.save(ProductJpaEntity.builder().name("Notebook").price(new BigDecimal("12.50")).build());

    Order saved = orderPersistenceAdapter.save(new Order(
        null,
        user.getId(),
        user.getName(),
        OrderStatus.CREATED,
        new BigDecimal("25.00"),
        OffsetDateTime.parse("2026-03-24T10:15:30Z"),
        List.of(new OrderItem(null, product.getId(), product.getName(), 2, new BigDecimal("12.50"), new BigDecimal("25.00")))
    ));

    assertThat(saved.id()).isNotNull();
    assertThat(saved.items()).hasSize(1);
    assertThat(orderPersistenceAdapter.findAll()).hasSize(1);
  }
}
