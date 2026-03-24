package br.com.alr.api.sbkafkaproducersample.application.usecase;

import br.com.alr.api.sbkafkaproducersample.application.exception.NotFoundException;
import br.com.alr.api.sbkafkaproducersample.application.port.out.UserQueryPort;
import br.com.alr.api.sbkafkaproducersample.domain.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoadOrderUserServiceTest {

  @Mock
  private UserQueryPort userQueryPort;

  @Test
  void shouldReturnUserWhenItExists() {
    LoadOrderUserService service = new LoadOrderUserService(userQueryPort);
    User expectedUser = new User(1L, "User", "user@example.com");
    when(userQueryPort.findById(1L)).thenReturn(Optional.of(expectedUser));

    User user = service.loadById(1L);

    assertThat(user).isEqualTo(expectedUser);
  }

  @Test
  void shouldFailWhenUserDoesNotExist() {
    LoadOrderUserService service = new LoadOrderUserService(userQueryPort);
    when(userQueryPort.findById(1L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.loadById(1L))
        .isInstanceOf(NotFoundException.class)
        .hasMessageContaining("User not found");
  }
}
