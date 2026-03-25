package br.com.alr.api.sbkafkaproducersample.adapter.out.messaging;

import br.com.alr.api.sbkafkaproducersample.application.port.out.EmailSenderPort;
import br.com.alr.api.sbkafkaproducersample.domain.model.InvoiceEmail;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
public class SmtpInvoiceEmailSender implements EmailSenderPort {

  private final JavaMailSender javaMailSender;
  private final String fromAddress;

  public SmtpInvoiceEmailSender(JavaMailSender javaMailSender, @Value("${app.mail.from}") String fromAddress) {
    this.javaMailSender = javaMailSender;
    this.fromAddress = fromAddress;
  }

  @Override
  public void send(InvoiceEmail invoiceEmail) {
    SimpleMailMessage message = new SimpleMailMessage();
    message.setFrom(fromAddress);
    message.setTo(invoiceEmail.recipientEmail());
    message.setSubject(invoiceEmail.subject());
    message.setText(invoiceEmail.body());
    javaMailSender.send(message);
  }
}
