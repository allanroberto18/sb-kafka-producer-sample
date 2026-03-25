package br.com.alr.api.sbkafkaproducersample.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration
public class MailConfiguration {

  @Bean
  JavaMailSender javaMailSender(
      @Value("${spring.mail.host}") String host,
      @Value("${spring.mail.port}") int port,
      @Value("${spring.mail.username:}") String username,
      @Value("${spring.mail.password:}") String password,
      @Value("${spring.mail.properties.mail.smtp.auth:false}") boolean smtpAuth,
      @Value("${spring.mail.properties.mail.smtp.starttls.enable:false}") boolean startTls
  ) {
    JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
    mailSender.setHost(host);
    mailSender.setPort(port);
    mailSender.setUsername(username);
    mailSender.setPassword(password);
    Properties properties = new Properties();
    properties.put("mail.smtp.auth", smtpAuth);
    properties.put("mail.smtp.starttls.enable", startTls);
    mailSender.setJavaMailProperties(properties);
    return mailSender;
  }
}
