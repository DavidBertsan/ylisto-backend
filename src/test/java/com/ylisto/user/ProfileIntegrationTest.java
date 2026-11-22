package com.ylisto.user;

import com.ylisto.TestcontainersConfiguration;
import com.ylisto.signup.SignupRequest;
import com.ylisto.signup.SignupResponse;
import com.ylisto.signup.SignupService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Real database: profile changes must actually reach the table (dirty checking needs @Transactional),
 * and passwords are only ever stored hashed.
 */
@SpringBootTest
@Import(TestcontainersConfiguration.class)
class ProfileIntegrationTest {

	private static final String OLD_PASSWORD = "a-long-password";

	@Autowired
	private SignupService signupService;

	@Autowired
	private ProfileService profileService;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private JdbcTemplate jdbc;

	private SignupResponse pepe;

	@BeforeEach
	void signup() {
		pepe = signupService.signup(new SignupRequest("Carniceria Pepe", "Butcher", "pepe",
				OLD_PASSWORD, "Pepe", "600000000", null));
	}

	@AfterEach
	void cleanDatabase() {
		jdbc.update("delete from clocking");
		jdbc.update("delete from users");
		jdbc.update("delete from business");
	}

	@Test
	void shouldPersistTheNewProfile() {
		profileService.updateProfile(pepe.userId(), "Pepe Garcia", "611111111", "https://img/pepe.png");

		// Read straight from the table: without @Transactional the change would only live in memory
		var row = jdbc.queryForMap("select name, mobile, image_url from users where id = ?", pepe.userId());
		assertThat(row).containsEntry("name", "Pepe Garcia")
				.containsEntry("mobile", "611111111")
				.containsEntry("image_url", "https://img/pepe.png");
	}

	@Test
	void shouldStoreTheNewPasswordHashed() {
		profileService.changePassword(pepe.userId(), OLD_PASSWORD, "a-brand-new-password");

		String stored = storedPassword();
		assertThat(stored).isNotEqualTo("a-brand-new-password");
		assertThat(passwordEncoder.matches("a-brand-new-password", stored)).isTrue();
		assertThat(passwordEncoder.matches(OLD_PASSWORD, stored)).isFalse();
	}

	@Test
	void shouldKeepTheOldPasswordWhenTheCurrentOneIsWrong() {
		String before = storedPassword();

		assertThatThrownBy(() -> profileService.changePassword(pepe.userId(), "not-my-password", "a-brand-new-password"))
				.isInstanceOf(InvalidPasswordException.class);

		assertThat(storedPassword()).isEqualTo(before);
	}

	private String storedPassword() {
		return jdbc.queryForObject("select password from users where id = ?", String.class, pepe.userId());
	}
}
