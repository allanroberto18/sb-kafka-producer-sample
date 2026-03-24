package br.com.alr.api.sbkafkaproducersample.application.usecase;

import br.com.alr.api.sbkafkaproducersample.application.port.in.ListUsersUseCase;
import br.com.alr.api.sbkafkaproducersample.application.port.out.UserQueryPort;
import br.com.alr.api.sbkafkaproducersample.domain.model.User;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ListUsersService implements ListUsersUseCase {

  private final UserQueryPort userQueryPort;

  public ListUsersService(UserQueryPort userQueryPort) {
    this.userQueryPort = userQueryPort;
  }

  @Override
  public List<User> findAll() {
    return userQueryPort.findAll();
  }
}
