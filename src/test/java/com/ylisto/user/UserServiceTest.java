package com.ylisto.user;

import com.ylisto.business.Business;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Plain unit test: no Spring context, so it runs in milliseconds.
 * The repository is mocked (it is the database boundary); the password
 * encoder is the REAL BCrypt, because hashing is the behavior we want to prove.
 */
class UserServiceTest {

	private UserRepository userRepository;
	private PasswordEncoder passwordEncoder;
	private UserService userService;

	private final Business business = new Business("Carniceria Pepe", "Butcher");

	@BeforeEach
	void setUp() {
		userRepository = mock(UserRepository.class);
		passwordEncoder = new BCryptPasswordEncoder();
		userService = new UserService(userRepository, passwordEncoder);
	}

	@Test
	void shouldStoreHashedPasswordWhenRegistering() {
		// Given: the username is free, and save() returns whatever it receives
		when(userRepository.existsByUsername("ana")).thenReturn(false);
		when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

		// When
		User saved = userService.register("ana", "secret123", business, "Ana", "600000000", Role.EMPLOYEE, null);

		// Then: the stored password is a hash of the raw one, never the raw one itself
		assertThat(saved.getPassword()).isNotEqualTo("secret123");
		assertThat(passwordEncoder.matches("secret123", saved.getPassword())).isTrue();
		assertThat(saved.getUsername()).isEqualTo("ana");
		assertThat(saved.getBusiness()).isSameAs(business);
		assertThat(saved.getRole()).isEqualTo(Role.EMPLOYEE);
	}

	@Test
	void shouldThrowAndNotSaveWhenUsernameAlreadyExists() {
		// Given
		when(userRepository.existsByUsername("ana")).thenReturn(true);

		// When + Then
		assertThatThrownBy(() ->
				userService.register("ana", "secret123", business, "Ana", "600000000", Role.EMPLOYEE, null))
				.isInstanceOf(UsernameAlreadyExistsException.class)
				.hasMessageContaining("ana");

		verify(userRepository, never()).save(any());
	}
}
