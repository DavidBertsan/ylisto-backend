package com.ylisto.user;

import com.ylisto.business.Business;
import com.ylisto.security.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProfileController.class)
@Import(SecurityConfig.class)
class ProfileControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private ProfileService profileService;

	private final UUID userId = UUID.randomUUID();
	private final User ana = new User("ana", "$2a$10$secret-hash", new Business("Carniceria Pepe", "Butcher"),
			"Ana", "600000000", Role.EMPLOYEE, null);

	@Test
	void shouldReturnMyProfileWithoutThePassword() throws Exception {
		when(profileService.getProfile(userId)).thenReturn(ana);

		mockMvc.perform(get("/profile").with(token()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.username").value("ana"))
				.andExpect(jsonPath("$.role").value("EMPLOYEE"))
				.andExpect(jsonPath("$.password").doesNotExist());
	}

	@Test
	void shouldReturn401WithoutAToken() throws Exception {
		mockMvc.perform(get("/profile"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void shouldUpdateMyProfileUsingTheIdFromTheToken() throws Exception {
		when(profileService.updateProfile(userId, "Ana Garcia", "611111111", null)).thenReturn(ana);

		mockMvc.perform(put("/profile").with(token())
						.contentType(MediaType.APPLICATION_JSON).content("""
								{ "name": "Ana Garcia", "mobile": "611111111" }
								"""))
				.andExpect(status().isOk());

		verify(profileService).updateProfile(userId, "Ana Garcia", "611111111", null);
	}

	@Test
	void shouldReturn400WhenNameIsBlank() throws Exception {
		mockMvc.perform(put("/profile").with(token())
						.contentType(MediaType.APPLICATION_JSON).content("""
								{ "name": " ", "mobile": "611111111" }
								"""))
				.andExpect(status().isBadRequest());

		verify(profileService, never()).updateProfile(any(), any(), any(), any());
	}

	@Test
	void shouldReturn204WhenPasswordChanges() throws Exception {
		mockMvc.perform(put("/profile/password").with(token())
						.contentType(MediaType.APPLICATION_JSON).content(passwordBody("old-long-password", "new-long-password")))
				.andExpect(status().isNoContent())
				.andExpect(content().string(""));

		verify(profileService).changePassword(userId, "old-long-password", "new-long-password");
	}

	@Test
	void shouldReturn400NotA401WhenCurrentPasswordIsWrong() throws Exception {
		doThrow(new InvalidPasswordException("Current password is incorrect"))
				.when(profileService).changePassword(any(), any(), any());

		mockMvc.perform(put("/profile/password").with(token())
						.contentType(MediaType.APPLICATION_JSON).content(passwordBody("wrong-password!", "new-long-password")))
				.andExpect(status().isBadRequest());
	}

	@Test
	void shouldReturn400WhenNewPasswordIsTooShort() throws Exception {
		mockMvc.perform(put("/profile/password").with(token())
						.contentType(MediaType.APPLICATION_JSON).content(passwordBody("old-long-password", "short")))
				.andExpect(status().isBadRequest());

		verify(profileService, never()).changePassword(any(), any(), any());
	}

	private String passwordBody(String current, String next) {
		return """
				{ "currentPassword": "%s", "newPassword": "%s" }
				""".formatted(current, next);
	}

	private RequestPostProcessor token() {
		return jwt()
				.jwt(t -> t.subject(userId.toString()).claim("businessId", UUID.randomUUID().toString()).claim("role", "EMPLOYEE"))
				.authorities(new SimpleGrantedAuthority("ROLE_EMPLOYEE"));
	}
}
