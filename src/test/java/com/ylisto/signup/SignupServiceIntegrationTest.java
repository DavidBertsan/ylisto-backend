package com.ylisto.signup;

import com.ylisto.TestcontainersConfiguration;
import com.ylisto.user.Role;
import com.ylisto.user.UsernameAlreadyExistsException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Full context + real Postgres. Deliberately NOT @Transactional: if the test
 * method were wrapped in a transaction, the service would join it and we could
 * not see whether its own rollback really happened. We clean up by hand instead.
 */
@SpringBootTest
@Import(TestcontainersConfiguration.class)
class SignupServiceIntegrationTest {

	@Autowired
	private SignupService signupService;

	@Autowired
	private JdbcTemplate jdbc;

	@AfterEach
	void cleanDatabase() {
		jdbc.update("delete from clocking");
		jdbc.update("delete from users");
		jdbc.update("delete from business");
	}

	@Test
	void shouldCreateBusinessAndAdminWithHashedPassword() {
		SignupResponse response = signupService.signup(request("Carniceria Pepe", "pepe"));

		assertThat(response.businessId()).isNotNull();
		assertThat(response.userId()).isNotNull();
		assertThat(response.role()).isEqualTo(Role.ADMIN);

		String storedPassword = jdbc.queryForObject(
				"select password from users where id = ?", String.class, response.userId());
		assertThat(storedPassword).isNotEqualTo("a-long-password").startsWith("$2");
	}

	@Test
	void shouldRollBackBusinessWhenUsernameIsTaken() {
		signupService.signup(request("Carniceria Pepe", "pepe"));

		assertThatThrownBy(() -> signupService.signup(request("Bar La Esquina", "pepe")))
				.isInstanceOf(UsernameAlreadyExistsException.class);

		// The second business was saved before the user failed: the rollback must remove it
		Integer orphanBusinesses = jdbc.queryForObject(
				"select count(*) from business where name = 'Bar La Esquina'", Integer.class);
		assertThat(orphanBusinesses).isZero();
	}

	private SignupRequest request(String businessName, String username) {
		return new SignupRequest(businessName, "Some type", username, "a-long-password",
				"Owner", "600000000", null);
	}
}
