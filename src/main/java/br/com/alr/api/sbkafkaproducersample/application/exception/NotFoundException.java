package br.com.alr.api.sbkafkaproducersample.application.exception;

public class NotFoundException extends RuntimeException {

  public NotFoundException(String message) {
    super(message);
  }
}
