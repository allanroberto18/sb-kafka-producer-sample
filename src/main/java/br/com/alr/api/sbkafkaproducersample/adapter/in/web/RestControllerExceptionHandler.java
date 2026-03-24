package br.com.alr.api.sbkafkaproducersample.adapter.in.web;

import br.com.alr.api.sbkafkaproducersample.application.exception.BusinessValidationException;
import br.com.alr.api.sbkafkaproducersample.application.exception.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.util.List;

@RestControllerAdvice
public class RestControllerExceptionHandler {

  @ExceptionHandler(NotFoundException.class)
  public ProblemDetail handleNotFound(NotFoundException exception) {
    ProblemDetail detail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
    detail.setTitle("Resource Not Found");
    detail.setType(URI.create("https://example.com/problems/not-found"));
    return detail;
  }

  @ExceptionHandler({BusinessValidationException.class, MethodArgumentNotValidException.class})
  public ProblemDetail handleBadRequest(Exception exception) {
    ProblemDetail detail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
    detail.setTitle("Bad Request");
    detail.setType(URI.create("https://example.com/problems/bad-request"));

    if (exception instanceof MethodArgumentNotValidException validationException) {
      detail.setDetail("Payload validation failed");
      List<String> errors = validationException.getBindingResult()
          .getFieldErrors()
          .stream()
          .map(this::formatFieldError)
          .toList();
      detail.setProperty("errors", errors);
      return detail;
    }

    detail.setDetail(exception.getMessage());
    return detail;
  }

  private String formatFieldError(FieldError error) {
    return error.getField() + ": " + error.getDefaultMessage();
  }
}
