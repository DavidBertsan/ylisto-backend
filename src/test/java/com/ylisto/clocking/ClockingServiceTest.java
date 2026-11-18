package com.ylisto.clocking;

import com.ylisto.business.Business;
import com.ylisto.user.Role;
import com.ylisto.user.User;
import com.ylisto.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit test with a FROZEN clock: Instant.now(clock) always returns NOW,
 * so we can assert exact clock-in/clock-out times.
 */
class ClockingServiceTest {

	private static final Instant NOW = Instant.parse("2026-11-18T08:00:00Z");

	private ClockingRepository clockingRepository;
	private UserService userService;
	private ClockingService clockingService;

	private final UUID userId = UUID.randomUUID();
	private final User user = new User("ana", "hash", new Business("Bar La Esquina", "Bar"),
			"Ana", "600000000", Role.EMPLOYEE, null);

	@BeforeEach
	void setUp() {
		clockingRepository = mock(ClockingRepository.class);
		userService = mock(UserService.class);
		Clock fixedClock = Clock.fixed(NOW, ZoneOffset.UTC);
		clockingService = new ClockingService(clockingRepository, userService, fixedClock);

		when(clockingRepository.save(any(Clocking.class))).thenAnswer(invocation -> invocation.getArgument(0));
	}

	@Test
	void shouldClockInAtServerTimeWhenNoOpenClocking() {
		when(clockingRepository.findByUserIdAndClockOutIsNull(userId)).thenReturn(Optional.empty());
		when(userService.findById(userId)).thenReturn(user);

		Clocking clocking = clockingService.clockIn(userId);

		assertThat(clocking.getUser()).isSameAs(user);
		assertThat(clocking.getClockIn()).isEqualTo(NOW);
		assertThat(clocking.getClockOut()).isNull();
	}

	@Test
	void shouldThrowAndNotSaveWhenClockingInTwice() {
		Clocking open = new Clocking(user, NOW.minusSeconds(3600));
		when(clockingRepository.findByUserIdAndClockOutIsNull(userId)).thenReturn(Optional.of(open));

		assertThatThrownBy(() -> clockingService.clockIn(userId))
				.isInstanceOf(AlreadyClockedInException.class);

		verify(clockingRepository, never()).save(any());
	}

	@Test
	void shouldCloseTheOpenClockingWhenClockingOut() {
		Instant nineHoursAgo = NOW.minusSeconds(9 * 3600);
		Clocking open = new Clocking(user, nineHoursAgo);
		when(clockingRepository.findByUserIdAndClockOutIsNull(userId)).thenReturn(Optional.of(open));

		Clocking closed = clockingService.clockOut(userId);

		// Same row closed, not a new one created
		assertThat(closed).isSameAs(open);
		assertThat(closed.getClockIn()).isEqualTo(nineHoursAgo);
		assertThat(closed.getClockOut()).isEqualTo(NOW);
	}

	@Test
	void shouldThrowAndNotSaveWhenClockingOutWithoutOpenClocking() {
		when(clockingRepository.findByUserIdAndClockOutIsNull(userId)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> clockingService.clockOut(userId))
				.isInstanceOf(NotClockedInException.class);

		verify(clockingRepository, never()).save(any());
	}
}
