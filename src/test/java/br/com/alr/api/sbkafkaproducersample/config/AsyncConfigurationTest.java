package br.com.alr.api.sbkafkaproducersample.config;

import br.com.alr.api.sbkafkaproducersample.support.PostgresContainerIT;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.task.AsyncTaskExecutor;

import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class AsyncConfigurationTest extends PostgresContainerIT {

  private final AsyncTaskExecutor taskExecutor;

  AsyncConfigurationTest(@Qualifier("taskExecutor") AsyncTaskExecutor taskExecutor) {
    this.taskExecutor = taskExecutor;
  }

  @Test
  void shouldExecuteAsyncTasksOnVirtualThreads() throws ExecutionException, InterruptedException {
    boolean virtualThread = taskExecutor.submitCompletable(() -> Thread.currentThread().isVirtual()).get();

    assertThat(virtualThread).isTrue();
  }
}
