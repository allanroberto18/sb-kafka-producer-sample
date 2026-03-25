package br.com.alr.api.sbkafkaproducersample.support;

import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

public abstract class PostgresContainerIT {

  @ServiceConnection
  private static final PostgreSQLContainer<?> PG_CONTAINER = new PostgreSQLContainer<>("postgres:17-alpine")
      .withDatabaseName("orders_test_db")
      .withUsername("postgres")
      .withPassword("postgres");

  private static final KafkaContainer KA_CONTAINER = new KafkaContainer(
      DockerImageName.parse("apache/kafka-native:4.1.0")
  );

  static {
    PG_CONTAINER.start();
    KA_CONTAINER.start();
  }

  @DynamicPropertySource
  static void configureProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.kafka.bootstrap-servers", KA_CONTAINER::getBootstrapServers);
  }
}
