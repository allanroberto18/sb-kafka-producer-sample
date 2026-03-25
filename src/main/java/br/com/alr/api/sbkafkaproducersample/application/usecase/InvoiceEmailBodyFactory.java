package br.com.alr.api.sbkafkaproducersample.application.usecase;

import br.com.alr.api.sbkafkaproducersample.domain.model.OrderCreatedEvent;
import br.com.alr.api.sbkafkaproducersample.domain.model.OrderCreatedItemEvent;
import br.com.alr.api.sbkafkaproducersample.domain.model.User;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class InvoiceEmailBodyFactory {

  public String create(User user, OrderCreatedEvent event) {
    StringBuilder body = new StringBuilder();
    body.append("Invoice for order #").append(event.orderId()).append("\n\n");
    body.append("Hello ").append(user.name()).append(",\n\n");
    body.append("Thank you for your order. Here is your invoice summary.\n\n");
    body.append("Items\n");
    body.append("-----\n");
    for (OrderCreatedItemEvent item : event.items()) {
      body.append(item.productName())
          .append(" | qty: ").append(item.quantity())
          .append(" | unit: ").append(formatAmount(item.unitPrice()))
          .append(" | total: ").append(formatAmount(item.lineTotal()))
          .append("\n");
    }
    body.append("\n");
    body.append("Order total: ").append(formatAmount(event.totalAmount())).append("\n");
    body.append("Issued at: ").append(event.createdAt()).append("\n");
    return body.toString();
  }

  private String formatAmount(BigDecimal amount) {
    return "$" + amount.setScale(2, RoundingMode.HALF_UP);
  }
}
