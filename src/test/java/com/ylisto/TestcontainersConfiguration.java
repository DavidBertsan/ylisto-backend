package com.ylisto;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.postgresql.PostgreSQLContainer;

/**
 * Starts a throwaway PostgreSQL in Docker for integration tests.
 * Import it with @Import(TestcontainersConfiguration.class) in any
 * @SpringBootTest or @DataJpaTest that needs a real database.
 */
@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfiguration {

	@Bean
	@ServiceConnection
	PostgreSQLContainer postgres() {
		return new PostgreSQLContainer("postgres:17");
	}
}
