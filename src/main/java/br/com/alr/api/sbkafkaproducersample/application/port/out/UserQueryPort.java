package br.com.alr.api.sbkafkaproducersample.application.port.out;

import br.com.alr.api.sbkafkaproducersample.domain.model.User;

import java.util.List;
import java.util.Optional;

public interface UserQueryPort {

  List<User> findAll();

  Optional<User> findById(Long id);
}
