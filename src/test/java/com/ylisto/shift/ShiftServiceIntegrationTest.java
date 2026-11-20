package com.ylisto.shift;

import com.ylisto.TestcontainersConfiguration;
import com.ylisto.signup.SignupRequest;
import com.ylisto.signup.SignupResponse;
import com.ylisto.signup.SignupService;
import com.ylisto.team.CreateEmployeeRequest;
import com.ylisto.team.TeamMemberResponse;
import com.ylisto.team.TeamService;
import com.ylisto.user.UserNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Real PostgreSQL: proves the overlap query (and its parameter order) case by case.
 * Existing shift in every test: Lucia, 09:00 → 14:00 on 2026-11-24.
 */
@SpringBootTest
@Import(TestcontainersConfiguration.class)
class ShiftServiceIntegrationTest {

	@Autowired
	private SignupService signupService;

	@Autowired
	private TeamService teamService;

	@Autowired
	private ShiftService shiftService;

	@Autowired
	private JdbcTemplate jdbc;

	private SignupResponse bar;
	private TeamMemberResponse lucia;

	@BeforeEach
	void setUp() {
		bar = signup("Bar La Esquina", "manolo");
		lucia = teamService.addEmployee(bar.businessId(), employee("lucia"));
		shiftService.assign(bar.businessId(), lucia.id(), at("09:00"), at("14:00"));
	}

	@AfterEach
	void cleanDatabase() {
		jdbc.update("delete from shift");
		jdbc.update("delete from clocking");
		jdbc.update("delete from users");
		jdbc.update("delete from business");
	}

	@ParameterizedTest(name = "{0}–{1} overlaps 09:00–14:00")
	@CsvSource({
			"12:00, 18:00",   // starts inside, ends after
			"07:00, 09:30",   // starts before, ends inside
			"10:00, 11:00",   // fully inside
			"08:00, 15:00",   // wraps the whole shift
			"09:00, 14:00",   // exactly the same
	})
	void shouldRejectOverlappingShifts(String start, String end) {
		assertThatThrownBy(() -> shiftService.assign(bar.businessId(), lucia.id(), at(start), at(end)))
				.isInstanceOf(ShiftOverlapException.class);
	}

	@ParameterizedTest(name = "{0}–{1} does not overlap 09:00–14:00")
	@CsvSource({
			"14:00, 18:00",   // starts exactly when the other ends: a normal shift change
			"06:00, 09:00",   // ends exactly when the other starts
			"15:00, 20:00",   // completely after
	})
	void shouldAcceptShiftsThatOnlyTouchOrAreApart(String start, String end) {
		assertThatCode(() -> shiftService.assign(bar.businessId(), lucia.id(), at(start), at(end)))
				.doesNotThrowAnyException();
	}

	@Test
	void shouldLetAnotherEmployeeWorkAtTheSameTime() {
		TeamMemberResponse pablo = teamService.addEmployee(bar.businessId(), employee("pablo"));

		assertThatCode(() -> shiftService.assign(bar.businessId(), pablo.id(), at("09:00"), at("14:00")))
				.doesNotThrowAnyException();
	}

	@Test
	void shouldRejectAShiftThatEndsBeforeItStarts() {
		assertThatThrownBy(() -> shiftService.assign(bar.businessId(), lucia.id(), at("18:00"), at("16:00")))
				.isInstanceOf(InvalidShiftException.class);
	}

	@Test
	void shouldNotAssignShiftsToAnEmployeeOfAnotherBusiness() {
		SignupResponse butcher = signup("Carniceria Pepe", "pepe");

		assertThatThrownBy(() -> shiftService.assign(butcher.businessId(), lucia.id(), at("15:00"), at("20:00")))
				.isInstanceOf(UserNotFoundException.class);
	}

	@Test
	void shouldNotAssignShiftsToADeactivatedEmployee() {
		teamService.remove(bar.businessId(), bar.userId(), lucia.id());

		assertThatThrownBy(() -> shiftService.assign(bar.businessId(), lucia.id(), at("15:00"), at("20:00")))
				.isInstanceOf(UserNotFoundException.class);
	}

	@Test
	void shouldReturnMyScheduleOrderedAndWithinTheRange() {
		shiftService.assign(bar.businessId(), lucia.id(), at("15:00"), at("18:00"));
		shiftService.assign(bar.businessId(), lucia.id(), Instant.parse("2026-12-01T09:00:00Z"), Instant.parse("2026-12-01T14:00:00Z"));

		assertThat(shiftService.mySchedule(lucia.id(), at("00:00"), at("23:59")))
				.extracting(Shift::getStartsAt)
				.containsExactly(at("09:00"), at("15:00"));
	}

	@Test
	void shouldNotRemoveAShiftOfAnotherBusiness() {
		SignupResponse butcher = signup("Carniceria Pepe", "pepe");
		Shift luciaShift = shiftService.mySchedule(lucia.id(), at("00:00"), at("23:59")).getFirst();

		assertThatThrownBy(() -> shiftService.remove(butcher.businessId(), luciaShift.getId()))
				.isInstanceOf(ShiftNotFoundException.class);
	}

	private static Instant at(String hhmm) {
		return Instant.parse("2026-11-24T" + hhmm + ":00Z");
	}

	private SignupResponse signup(String businessName, String username) {
		return signupService.signup(new SignupRequest(businessName, "Some type", username,
				"a-long-password", "Owner", "600000000", null));
	}

	private CreateEmployeeRequest employee(String username) {
		return new CreateEmployeeRequest(username, "a-long-password", "Employee", "611111111", null);
	}
}
