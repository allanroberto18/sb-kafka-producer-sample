package br.com.alr.api.sbkafkaproducersample.application.usecase;

import br.com.alr.api.sbkafkaproducersample.domain.enumtype.OrderStatus;
import br.com.alr.api.sbkafkaproducersample.domain.model.OrderCreatedEvent;
import br.com.alr.api.sbkafkaproducersample.domain.model.OrderCreatedItemEvent;
import br.com.alr.api.sbkafkaproducersample.domain.model.User;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class InvoiceEmailBodyFactoryTest {

  private final InvoiceEmailBodyFactory invoiceEmailBodyFactory = new InvoiceEmailBodyFactory();

  @Test
  void shouldBuildInvoiceLikeBodyWithUserNameItemsAndTotal() {
    String body = invoiceEmailBodyFactory.create(
        new User(1L, "Default User", "default.user@example.com"),
        new OrderCreatedEvent(
            99L,
            1L,
            "Ignored Name",
            OrderStatus.CREATED,
            new BigDecimal("70.90"),
            OffsetDateTime.parse("2026-03-24T12:00:00Z"),
            List.of(
                new OrderCreatedItemEvent(10L, "Notebook", 2, new BigDecimal("12.50"), new BigDecimal("25.00")),
                new OrderCreatedItemEvent(20L, "Keyboard", 1, new BigDecimal("45.90"), new BigDecimal("45.90"))
            )
        )
    );

    assertThat(body).contains("Hello Default User");
    assertThat(body).contains("Notebook | qty: 2 | unit: $12.50 | total: $25.00");
    assertThat(body).contains("Keyboard | qty: 1 | unit: $45.90 | total: $45.90");
    assertThat(body).contains("Order total: $70.90");
    assertThat(body).contains("Invoice for order #99");
  }
}
