package br.com.alr.api.sbkafkaproducersample.support;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

public abstract class PostgresContainerIT {

  private static final PostgreSQLContainer<?> POSTGRESQL_CONTAINER = new PostgreSQLContainer<>("postgres:17-alpine")
      .withDatabaseName("orders_test_db")
      .withUsername("postgres")
      .withPassword("postgres");

  private static final KafkaContainer KAFKA_CONTAINER = new KafkaContainer(DockerImageName.parse("apache/kafka-native:4.1.0"));

  static {
    POSTGRESQL_CONTAINER.start();
    KAFKA_CONTAINER.start();
  }

  @DynamicPropertySource
  static void configureProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", POSTGRESQL_CONTAINER::getJdbcUrl);
    registry.add("spring.datasource.username", POSTGRESQL_CONTAINER::getUsername);
    registry.add("spring.datasource.password", POSTGRESQL_CONTAINER::getPassword);
    registry.add("spring.datasource.driver-class-name", POSTGRESQL_CONTAINER::getDriverClassName);
    registry.add("spring.flyway.url", POSTGRESQL_CONTAINER::getJdbcUrl);
    registry.add("spring.flyway.user", POSTGRESQL_CONTAINER::getUsername);
    registry.add("spring.flyway.password", POSTGRESQL_CONTAINER::getPassword);
    registry.add("spring.kafka.bootstrap-servers", KAFKA_CONTAINER::getBootstrapServers);
  }
}
