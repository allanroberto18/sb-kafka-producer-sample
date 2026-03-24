package br.com.alr.api.sbkafkaproducersample.application.usecase;

import br.com.alr.api.sbkafkaproducersample.application.exception.NotFoundException;
import br.com.alr.api.sbkafkaproducersample.application.port.out.UserQueryPort;
import br.com.alr.api.sbkafkaproducersample.domain.model.User;
import org.springframework.stereotype.Service;

@Service
public class LoadOrderUserService {

  private final UserQueryPort userQueryPort;

  public LoadOrderUserService(UserQueryPort userQueryPort) {
    this.userQueryPort = userQueryPort;
  }

  public User loadById(Long userId) {
    return userQueryPort.findById(userId)
        .orElseThrow(() -> new NotFoundException("User not found for id " + userId));
  }
}
