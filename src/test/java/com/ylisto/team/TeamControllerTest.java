package com.ylisto.team;

import com.ylisto.security.SecurityConfig;
import com.ylisto.user.Role;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TeamController.class)
@Import(SecurityConfig.class)
class TeamControllerTest {

	private static final String EMPLOYEE_BODY = """
			{ "username": "lucia", "password": "a-long-password", "name": "Lucia", "mobile": "611111111" }
			""";

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private TeamService teamService;

	private final UUID businessId = UUID.randomUUID();
	private final UUID adminId = UUID.randomUUID();

	@Test
	void shouldReturn403WhenEmployeeTriesToSeeTheTeam() throws Exception {
		mockMvc.perform(get("/team").with(token("EMPLOYEE")))
				.andExpect(status().isForbidden());

		verify(teamService, never()).list(any());
	}

	@Test
	void shouldListTheTeamOfTheBusinessInTheToken() throws Exception {
		when(teamService.list(businessId)).thenReturn(List.of(
				new TeamMemberResponse(UUID.randomUUID(), "lucia", "Lucia", "611111111", Role.EMPLOYEE)));

		mockMvc.perform(get("/team").with(token("ADMIN")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].username").value("lucia"))
				.andExpect(jsonPath("$[0].password").doesNotExist());
	}

	@Test
	void shouldReturn201WhenAdminAddsEmployee() throws Exception {
		when(teamService.addEmployee(eq(businessId), any())).thenReturn(
				new TeamMemberResponse(UUID.randomUUID(), "lucia", "Lucia", "611111111", Role.EMPLOYEE));

		mockMvc.perform(post("/team").with(token("ADMIN"))
						.contentType(MediaType.APPLICATION_JSON).content(EMPLOYEE_BODY))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.role").value("EMPLOYEE"));
	}

	@Test
	void shouldReturn400WhenEmployeePasswordIsTooShort() throws Exception {
		mockMvc.perform(post("/team").with(token("ADMIN"))
						.contentType(MediaType.APPLICATION_JSON).content(EMPLOYEE_BODY.replace("a-long-password", "short")))
				.andExpect(status().isBadRequest());

		verify(teamService, never()).addEmployee(any(), any());
	}

	@Test
	void shouldReturn204WhenAdminRemovesEmployee() throws Exception {
		UUID employeeId = UUID.randomUUID();

		mockMvc.perform(delete("/team/{userId}", employeeId).with(token("ADMIN")))
				.andExpect(status().isNoContent());

		verify(teamService).remove(businessId, adminId, employeeId);
	}

	@Test
	void shouldReturn409WhenAdminRemovesThemselves() throws Exception {
		doThrow(new CannotRemoveYourselfException("Cannot remove yourself"))
				.when(teamService).remove(businessId, adminId, adminId);

		mockMvc.perform(delete("/team/{userId}", adminId).with(token("ADMIN")))
				.andExpect(status().isConflict());
	}

	/** A token like the one TokenService issues: sub = adminId, businessId claim, and the role as authority. */
	private RequestPostProcessor token(String role) {
		return jwt()
				.jwt(t -> t.subject(adminId.toString()).claim("businessId", businessId.toString()).claim("role", role))
				.authorities(new SimpleGrantedAuthority("ROLE_" + role));
	}
}
