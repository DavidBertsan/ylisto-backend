package com.ylisto.clocking;

import com.ylisto.business.Business;
import com.ylisto.user.Role;
import com.ylisto.user.User;
import com.ylisto.user.UserNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Web slice: only the controller + GlobalExceptionHandler are loaded (no DB).
 * Proves the HTTP contract: URLs, status codes and JSON shape.
 */
@WebMvcTest(ClockingController.class)
class ClockingControllerTest {

	private static final Instant CLOCK_IN = Instant.parse("2026-11-18T08:00:00Z");
	private static final Instant CLOCK_OUT = Instant.parse("2026-11-18T16:00:00Z");

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private ClockingService clockingService;

	private final UUID userId = UUID.randomUUID();
	private final User user = new User("ana", "hash", new Business("Bar La Esquina", "Bar"),
			"Ana", "600000000", Role.EMPLOYEE, null);

	@Test
	void shouldReturn201WithOpenClockingWhenClockingIn() throws Exception {
		when(clockingService.clockIn(userId)).thenReturn(new Clocking(user, CLOCK_IN));

		mockMvc.perform(post("/users/{userId}/clock-in", userId))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.clockIn").value("2026-11-18T08:00:00Z"))
				.andExpect(jsonPath("$.clockOut").doesNotExist())
				.andExpect(jsonPath("$.user").doesNotExist());   // the DTO never leaks the User
	}

	@Test
	void shouldReturn409WhenAlreadyClockedIn() throws Exception {
		when(clockingService.clockIn(userId))
				.thenThrow(new AlreadyClockedInException("User is already clocked in: " + userId));

		mockMvc.perform(post("/users/{userId}/clock-in", userId))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.detail").value("User is already clocked in: " + userId));
	}

	@Test
	void shouldReturn404WhenUserDoesNotExist() throws Exception {
		when(clockingService.clockIn(userId))
				.thenThrow(new UserNotFoundException("User not found: " + userId));

		mockMvc.perform(post("/users/{userId}/clock-in", userId))
				.andExpect(status().isNotFound());
	}

	@Test
	void shouldReturn200WithClosedClockingWhenClockingOut() throws Exception {
		Clocking closed = new Clocking(user, CLOCK_IN);
		closed.clockOut(CLOCK_OUT);
		when(clockingService.clockOut(userId)).thenReturn(closed);

		mockMvc.perform(post("/users/{userId}/clock-out", userId))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.clockIn").value("2026-11-18T08:00:00Z"))
				.andExpect(jsonPath("$.clockOut").value("2026-11-18T16:00:00Z"));
	}

	@Test
	void shouldReturn404WhenClockingOutWithoutOpenClocking() throws Exception {
		when(clockingService.clockOut(userId))
				.thenThrow(new NotClockedInException("User is not clocked in: " + userId));

		mockMvc.perform(post("/users/{userId}/clock-out", userId))
				.andExpect(status().isNotFound());
	}

	@Test
	void shouldReturn400WhenUserIdIsNotAUuid() throws Exception {
		mockMvc.perform(post("/users/{userId}/clock-in", "not-a-uuid"))
				.andExpect(status().isBadRequest());
	}
}
