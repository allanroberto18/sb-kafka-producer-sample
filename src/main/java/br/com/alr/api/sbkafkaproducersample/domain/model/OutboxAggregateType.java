package br.com.alr.api.sbkafkaproducersample.domain.model;

public final class OutboxAggregateType {

  public static final String ORDER = "ORDER";
  public static final String INVOICE_EMAIL = "INVOICE_EMAIL";

  private OutboxAggregateType() {
  }
}
