package com.ylisto.shift;

import com.ylisto.business.Business;
import com.ylisto.security.SecurityConfig;
import com.ylisto.user.Role;
import com.ylisto.user.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ShiftController.class)
@Import(SecurityConfig.class)
class ShiftControllerTest {

	private static final Instant START = Instant.parse("2026-11-24T08:00:00Z");
	private static final Instant END = Instant.parse("2026-11-24T13:00:00Z");

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private ShiftService shiftService;

	private final UUID businessId = UUID.randomUUID();
	private final UUID userId = UUID.randomUUID();
	private final UUID employeeId = UUID.randomUUID();
	private final User lucia = new User("lucia", "hash", new Business("Bar", "Bar"), "Lucia", "611111111", Role.EMPLOYEE, null);

	private String assignBody() {
		return """
				{ "employeeId": "%s", "startsAt": "%s", "endsAt": "%s" }
				""".formatted(employeeId, START, END);
	}

	@Test
	void shouldReturn201WhenAdminAssignsAShift() throws Exception {
		when(shiftService.assign(businessId, employeeId, START, END)).thenReturn(new Shift(lucia, START, END));

		mockMvc.perform(post("/shifts").with(token("ADMIN")).contentType(MediaType.APPLICATION_JSON).content(assignBody()))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.startsAt").value("2026-11-24T08:00:00Z"));
	}

	@Test
	void shouldReturn403WhenEmployeeAssignsAShift() throws Exception {
		mockMvc.perform(post("/shifts").with(token("EMPLOYEE")).contentType(MediaType.APPLICATION_JSON).content(assignBody()))
				.andExpect(status().isForbidden());

		verify(shiftService, never()).assign(any(), any(), any(), any());
	}

	@Test
	void shouldReturn400WhenEndsAtIsMissing() throws Exception {
		mockMvc.perform(post("/shifts").with(token("ADMIN")).contentType(MediaType.APPLICATION_JSON)
						.content("{ \"employeeId\": \"%s\", \"startsAt\": \"%s\" }".formatted(employeeId, START)))
				.andExpect(status().isBadRequest());
	}

	@Test
	void shouldReturn409WhenShiftOverlaps() throws Exception {
		when(shiftService.assign(businessId, employeeId, START, END))
				.thenThrow(new ShiftOverlapException("Shift overlaps with an existing one"));

		mockMvc.perform(post("/shifts").with(token("ADMIN")).contentType(MediaType.APPLICATION_JSON).content(assignBody()))
				.andExpect(status().isConflict());
	}

	@Test
	void shouldReturn400WhenShiftEndsBeforeItStarts() throws Exception {
		when(shiftService.assign(businessId, employeeId, START, END))
				.thenThrow(new InvalidShiftException("Shift must end after it starts"));

		mockMvc.perform(post("/shifts").with(token("ADMIN")).contentType(MediaType.APPLICATION_JSON).content(assignBody()))
				.andExpect(status().isBadRequest());
	}

	@Test
	void shouldReturnMyScheduleUsingTheUserFromTheToken() throws Exception {
		when(shiftService.mySchedule(userId, START, END)).thenReturn(List.of(new Shift(lucia, START, END)));

		mockMvc.perform(get("/shifts/me").with(token("EMPLOYEE"))
						.param("from", START.toString()).param("to", END.toString()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].endsAt").value("2026-11-24T13:00:00Z"));

		verify(shiftService).mySchedule(userId, START, END);   // sub of the token, never a request param
	}

	@Test
	void shouldReturn204WhenAdminRemovesAShift() throws Exception {
		UUID shiftId = UUID.randomUUID();

		mockMvc.perform(delete("/shifts/{shiftId}", shiftId).with(token("ADMIN")))
				.andExpect(status().isNoContent());

		verify(shiftService).remove(businessId, shiftId);
	}

	@Test
	void shouldReturn403WhenEmployeeRemovesAShift() throws Exception {
		mockMvc.perform(delete("/shifts/{shiftId}", UUID.randomUUID()).with(token("EMPLOYEE")))
				.andExpect(status().isForbidden());

		verify(shiftService, never()).remove(any(), any());
	}

	private RequestPostProcessor token(String role) {
		return jwt()
				.jwt(t -> t.subject(userId.toString()).claim("businessId", businessId.toString()).claim("role", role))
				.authorities(new SimpleGrantedAuthority("ROLE_" + role));
	}
}
