package com.ylisto.team;

import com.ylisto.TestcontainersConfiguration;
import com.ylisto.signup.SignupRequest;
import com.ylisto.signup.SignupResponse;
import com.ylisto.signup.SignupService;
import com.ylisto.user.UserNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Real database: proves tenant isolation and that remove() persists
 * the deactivation through dirty checking (there is no save() call).
 */
@SpringBootTest
@Import(TestcontainersConfiguration.class)
class TeamServiceIntegrationTest {

	@Autowired
	private SignupService signupService;

	@Autowired
	private TeamService teamService;

	@Autowired
	private JdbcTemplate jdbc;

	@AfterEach
	void cleanDatabase() {
		jdbc.update("delete from clocking");
		jdbc.update("delete from users");
		jdbc.update("delete from business");
	}

	@Test
	void shouldDeactivateEmployeeAndHideItFromTheList() {
		SignupResponse butcher = signup("Carniceria Pepe", "pepe");
		TeamMemberResponse lucia = teamService.addEmployee(butcher.businessId(), employee("lucia"));

		teamService.remove(butcher.businessId(), butcher.userId(), lucia.id());

		// Dirty checking wrote the UPDATE: the row still exists (history kept) but is inactive
		Boolean active = jdbc.queryForObject("select active from users where id = ?", Boolean.class, lucia.id());
		assertThat(active).isFalse();
		assertThat(teamService.list(butcher.businessId()))
				.extracting(TeamMemberResponse::username)
				.containsExactly("pepe");
	}

	@Test
	void shouldNotLetAnAdminRemoveAnEmployeeOfAnotherBusiness() {
		SignupResponse butcher = signup("Carniceria Pepe", "pepe");
		SignupResponse bar = signup("Bar La Esquina", "manolo");
		TeamMemberResponse barEmployee = teamService.addEmployee(bar.businessId(), employee("lucia"));

		assertThatThrownBy(() -> teamService.remove(butcher.businessId(), butcher.userId(), barEmployee.id()))
				.isInstanceOf(UserNotFoundException.class);

		Boolean active = jdbc.queryForObject("select active from users where id = ?", Boolean.class, barEmployee.id());
		assertThat(active).isTrue();
	}

	@Test
	void shouldOnlyListTheTeamOfItsOwnBusiness() {
		SignupResponse butcher = signup("Carniceria Pepe", "pepe");
		SignupResponse bar = signup("Bar La Esquina", "manolo");
		teamService.addEmployee(bar.businessId(), employee("lucia"));

		assertThat(teamService.list(butcher.businessId()))
				.extracting(TeamMemberResponse::username)
				.containsExactly("pepe");
	}

	@Test
	void shouldNotLetAnAdminRemoveThemselves() {
		SignupResponse butcher = signup("Carniceria Pepe", "pepe");

		assertThatThrownBy(() -> teamService.remove(butcher.businessId(), butcher.userId(), butcher.userId()))
				.isInstanceOf(CannotRemoveYourselfException.class);
	}

	private SignupResponse signup(String businessName, String username) {
		return signupService.signup(new SignupRequest(businessName, "Some type", username,
				"a-long-password", "Owner", "600000000", null));
	}

	private CreateEmployeeRequest employee(String username) {
		return new CreateEmployeeRequest(username, "a-long-password", "Employee", "611111111", null);
	}
}
