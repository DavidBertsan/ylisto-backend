package com.ylisto.signup;

import com.ylisto.user.Role;
import com.ylisto.user.UsernameAlreadyExistsException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Web slice: proves the HTTP contract of POST /signup and that @Valid
 * blocks bad input BEFORE it reaches the service.
 */
@WebMvcTest(SignupController.class)
class SignupControllerTest {

	private static final String VALID_BODY = """
			{
			  "businessName": "Carniceria Pepe",
			  "businessType": "Butcher",
			  "username": "pepe",
			  "password": "a-long-password",
			  "name": "Pepe",
			  "mobile": "600000000"
			}
			""";

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private SignupService signupService;

	@Test
	void shouldReturn201WithIdsWhenSignupIsValid() throws Exception {
		UUID businessId = UUID.randomUUID();
		UUID userId = UUID.randomUUID();
		when(signupService.signup(any())).thenReturn(
				new SignupResponse(businessId, "Carniceria Pepe", userId, "pepe", Role.ADMIN));

		mockMvc.perform(post("/signup").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.businessId").value(businessId.toString()))
				.andExpect(jsonPath("$.userId").value(userId.toString()))
				.andExpect(jsonPath("$.role").value("ADMIN"))
				.andExpect(jsonPath("$.password").doesNotExist());
	}

	@Test
	void shouldReturn400AndNotCallServiceWhenPasswordIsTooShort() throws Exception {
		String body = VALID_BODY.replace("a-long-password", "short");

		mockMvc.perform(post("/signup").contentType(MediaType.APPLICATION_JSON).content(body))
				.andExpect(status().isBadRequest());

		verify(signupService, never()).signup(any());
	}

	@Test
	void shouldReturn400AndNotCallServiceWhenBusinessNameIsBlank() throws Exception {
		String body = VALID_BODY.replace("Carniceria Pepe", "");

		mockMvc.perform(post("/signup").contentType(MediaType.APPLICATION_JSON).content(body))
				.andExpect(status().isBadRequest());

		verify(signupService, never()).signup(any());
	}

	@Test
	void shouldReturn409WhenUsernameAlreadyExists() throws Exception {
		when(signupService.signup(any())).thenThrow(new UsernameAlreadyExistsException("Username already exists: pepe"));

		mockMvc.perform(post("/signup").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
				.andExpect(status().isConflict());
	}
}
