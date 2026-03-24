package br.com.alr.api.sbkafkaproducersample.application.port.in;

import br.com.alr.api.sbkafkaproducersample.domain.model.User;

import java.util.List;

public interface ListUsersUseCase {

  List<User> findAll();
}
