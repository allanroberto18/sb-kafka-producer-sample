package br.com.alr.api.sbkafkaproducersample.application.port.out;

import br.com.alr.api.sbkafkaproducersample.domain.model.InvoiceEmail;

public interface EmailSenderPort {

  void send(InvoiceEmail invoiceEmail);
}
