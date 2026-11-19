package com.ylisto.auth;

import com.jayway.jsonpath.JsonPath;
import com.ylisto.TestcontainersConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * End-to-end over HTTP with a real database and real security:
 * signup → login → use the token on a protected endpoint.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class AuthFlowIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JdbcTemplate jdbc;

	@AfterEach
	void cleanDatabase() {
		jdbc.update("delete from clocking");
		jdbc.update("delete from users");
		jdbc.update("delete from business");
	}

	@Test
	void shouldSignupLoginAndClockInWithTheToken() throws Exception {
		String signupBody = mockMvc.perform(post("/signup").contentType(MediaType.APPLICATION_JSON).content("""
						{ "businessName": "Carniceria Pepe", "username": "pepe",
						  "password": "a-long-password", "name": "Pepe", "mobile": "600000000" }
						"""))
				.andExpect(status().isCreated())
				.andReturn().getResponse().getContentAsString();
		String userId = JsonPath.read(signupBody, "$.userId");

		String loginBody = mockMvc.perform(post("/login").contentType(MediaType.APPLICATION_JSON).content("""
						{ "username": "pepe", "password": "a-long-password" }
						"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.token").isNotEmpty())
				.andReturn().getResponse().getContentAsString();
		String token = JsonPath.read(loginBody, "$.token");

		// No user id in the URL: the server takes it from the token
		mockMvc.perform(post("/clockings/clock-in").header("Authorization", "Bearer " + token))
				.andExpect(status().isCreated());

		Integer clockingsOfPepe = jdbc.queryForObject(
				"select count(*) from clocking where user_id = ?::uuid", Integer.class, userId);
		org.assertj.core.api.Assertions.assertThat(clockingsOfPepe).isEqualTo(1);
	}

	@Test
	void shouldReturn401WhenPasswordIsWrong() throws Exception {
		mockMvc.perform(post("/signup").contentType(MediaType.APPLICATION_JSON).content("""
						{ "businessName": "Carniceria Pepe", "username": "pepe",
						  "password": "a-long-password", "name": "Pepe", "mobile": "600000000" }
						"""))
				.andExpect(status().isCreated());

		mockMvc.perform(post("/login").contentType(MediaType.APPLICATION_JSON).content("""
						{ "username": "pepe", "password": "wrong-password" }
						"""))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void shouldReturn401WhenTokenIsForged() throws Exception {
		mockMvc.perform(post("/clockings/clock-in")
						.header("Authorization", "Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJoYWNrZXIifQ.fake-signature"))
				.andExpect(status().isUnauthorized());
	}
}
