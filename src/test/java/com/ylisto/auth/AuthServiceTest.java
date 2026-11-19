package com.ylisto.auth;

import com.ylisto.business.Business;
import com.ylisto.security.TokenService;
import com.ylisto.user.Role;
import com.ylisto.user.User;
import com.ylisto.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AuthServiceTest {

	private UserService userService;
	private TokenService tokenService;
	private AuthService authService;
	private User ana;

	@BeforeEach
	void setUp() {
		userService = mock(UserService.class);
		tokenService = mock(TokenService.class);
		PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
		authService = new AuthService(userService, passwordEncoder, tokenService);

		ana = new User("ana", passwordEncoder.encode("correct-password"), new Business("Bar", "Bar"),
				"Ana", "600000000", Role.EMPLOYEE, null);
	}

	@Test
	void shouldReturnTokenWhenCredentialsAreValid() {
		when(userService.findByUsername("ana")).thenReturn(Optional.of(ana));
		when(tokenService.issue(ana)).thenReturn("signed.jwt.token");

		LoginResponse response = authService.login(new LoginRequest("ana", "correct-password"));

		assertThat(response.token()).isEqualTo("signed.jwt.token");
	}

	@Test
	void shouldFailWithGenericMessageWhenPasswordIsWrong() {
		when(userService.findByUsername("ana")).thenReturn(Optional.of(ana));

		assertThatThrownBy(() -> authService.login(new LoginRequest("ana", "wrong-password")))
				.isInstanceOf(InvalidCredentialsException.class)
				.hasMessage("Invalid username or password");
	}

	@Test
	void shouldFailWithTheSameMessageWhenUserDoesNotExist() {
		when(userService.findByUsername("ghost")).thenReturn(Optional.empty());

		// Same exception + same message as a wrong password → no user enumeration
		assertThatThrownBy(() -> authService.login(new LoginRequest("ghost", "any-password")))
				.isInstanceOf(InvalidCredentialsException.class)
				.hasMessage("Invalid username or password");
	}
}
