package br.com.alr.api.sbkafkaproducersample.application.exception;

public class BusinessValidationException extends RuntimeException {

  public BusinessValidationException(String message) {
    super(message);
  }
}
