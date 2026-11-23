package com.ylisto.messaging;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Real database: one conversation per pair, only participants can read or write,
 * and polling returns only what is new.
 */
@SpringBootTest
@Import(TestcontainersConfiguration.class)
class MessagingIntegrationTest {

	@Autowired
	private SignupService signupService;

	@Autowired
	private TeamService teamService;

	@Autowired
	private ConversationService conversationService;

	@Autowired
	private ConversationRepository conversationRepository;

	@Autowired
	private JdbcTemplate jdbc;

	private SignupResponse pepe;      // admin of the butcher shop
	private TeamMemberResponse ana;   // employee of the butcher shop

	@BeforeEach
	void createTeam() {
		pepe = signup("Carniceria Pepe", "pepe");
		ana = teamService.addEmployee(pepe.businessId(),
				new CreateEmployeeRequest("ana", "a-long-password", "Ana", "611111111", null));
	}

	@AfterEach
	void cleanDatabase() {
		jdbc.update("delete from message");
		jdbc.update("delete from conversation");
		jdbc.update("delete from clocking");
		jdbc.update("delete from users");
		jdbc.update("delete from business");
	}

	@Test
	void shouldReturnTheSameConversationWhoeverOpensIt() {
		Conversation fromPepe = conversationService.openConversation(pepe.businessId(), pepe.userId(), ana.id());
		Conversation fromAna = conversationService.openConversation(pepe.businessId(), ana.id(), pepe.userId());

		assertThat(fromAna.getId()).isEqualTo(fromPepe.getId());
		assertThat(jdbc.queryForObject("select count(*) from conversation", Integer.class)).isEqualTo(1);
	}

	@Test
	void shouldRejectADuplicatePairAtTheDatabaseLevel() {
		Conversation existing = conversationService.openConversation(pepe.businessId(), pepe.userId(), ana.id());

		// Bypass the service: the unique constraint is the last line of defence
		Conversation duplicate = new Conversation(existing.getBusiness(),
				existing.getParticipantTwo(), existing.getParticipantOne(), Instant.now());
		assertThatThrownBy(() -> conversationRepository.saveAndFlush(duplicate))
				.isInstanceOf(DataIntegrityViolationException.class);
	}

	@Test
	void shouldNotOpenAConversationWithSomeoneFromAnotherBusiness() {
		SignupResponse bar = signup("Bar La Esquina", "manolo");

		assertThatThrownBy(() -> conversationService.openConversation(pepe.businessId(), pepe.userId(), bar.userId()))
				.isInstanceOf(UserNotFoundException.class);
	}

	@Test
	void shouldNotOpenAConversationWithAnInactiveUser() {
		teamService.remove(pepe.businessId(), pepe.userId(), ana.id());

		assertThatThrownBy(() -> conversationService.openConversation(pepe.businessId(), pepe.userId(), ana.id()))
				.isInstanceOf(UserNotFoundException.class);
	}

	@Test
	void shouldHideTheConversationFromOutsiders() {
		Conversation chat = conversationService.openConversation(pepe.businessId(), pepe.userId(), ana.id());
		SignupResponse outsider = signup("Bar La Esquina", "manolo");

		assertThatThrownBy(() -> conversationService.messages(outsider.userId(), chat.getId(), null))
				.isInstanceOf(ConversationNotFoundException.class);
		assertThatThrownBy(() -> conversationService.sendMessage(outsider.userId(), chat.getId(), "hola"))
				.isInstanceOf(ConversationNotFoundException.class);
	}

	@Test
	void shouldListTheConversationForBothParticipants() {
		conversationService.openConversation(pepe.businessId(), pepe.userId(), ana.id());

		assertThat(conversationService.myConversations(pepe.userId())).hasSize(1);
		assertThat(conversationService.myConversations(ana.id())).hasSize(1);
	}

	@Test
	void shouldPollOnlyTheNewMessagesInOrder() throws InterruptedException {
		Conversation chat = conversationService.openConversation(pepe.businessId(), pepe.userId(), ana.id());
		Message first = conversationService.sendMessage(pepe.userId(), chat.getId(), "Falta pollo");
		Thread.sleep(5);   // distinct timestamps, so "after" is unambiguous
		conversationService.sendMessage(ana.id(), chat.getId(), "Voy al almacen");
		Thread.sleep(5);
		conversationService.sendMessage(pepe.userId(), chat.getId(), "Gracias");

		assertThat(conversationService.messages(ana.id(), chat.getId(), null))
				.extracting(Message::getContent)
				.containsExactly("Falta pollo", "Voy al almacen", "Gracias");
		assertThat(conversationService.messages(ana.id(), chat.getId(), first.getSentAt()))
				.extracting(Message::getContent)
				.containsExactly("Voy al almacen", "Gracias");
	}

	private SignupResponse signup(String businessName, String username) {
		return signupService.signup(new SignupRequest(businessName, "Some type", username,
				"a-long-password", "Owner", "600000000", null));
	}
}
