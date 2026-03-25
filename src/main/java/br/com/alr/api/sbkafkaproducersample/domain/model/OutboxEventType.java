package br.com.alr.api.sbkafkaproducersample.domain.model;

public final class OutboxEventType {

  public static final String ORDER_CREATED = "ORDER_CREATED";
  public static final String EMAIL_INVOICE_REQUESTED = "EMAIL_INVOICE_REQUESTED";

  private OutboxEventType() {
  }
}
