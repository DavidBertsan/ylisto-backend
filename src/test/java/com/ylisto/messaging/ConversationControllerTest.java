package com.ylisto.messaging;

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
import org.springframework.test.util.ReflectionTestUtils;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ConversationController.class)
@Import(SecurityConfig.class)
class ConversationControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private ConversationService conversationService;

	private final UUID businessId = UUID.randomUUID();
	private final UUID userId = UUID.randomUUID();
	private final UUID recipientId = UUID.randomUUID();
	private final UUID conversationId = UUID.randomUUID();

	private final Business butcher = new Business("Carniceria Pepe", "Butcher");
	private final User me = user(userId, "ana");
	private final User pepe = user(recipientId, "pepe");
	private final Conversation chat = new Conversation(butcher, me, pepe, Instant.parse("2026-11-23T10:00:00Z"));
	private final Message hello = new Message(chat, me, "Falta pollo", Instant.parse("2026-11-23T10:01:00Z"));

	@Test
	void shouldOpenAConversationWith200AndNeverLeakUsers() throws Exception {
		when(conversationService.openConversation(businessId, userId, recipientId)).thenReturn(chat);

		mockMvc.perform(post("/conversations").with(token())
						.contentType(MediaType.APPLICATION_JSON).content("""
								{ "recipientId": "%s" }
								""".formatted(recipientId)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.participantOneId").exists())
				.andExpect(jsonPath("$.participantOne").doesNotExist());
	}

	@Test
	void shouldReturn400WhenRecipientIsMissing() throws Exception {
		mockMvc.perform(post("/conversations").with(token())
						.contentType(MediaType.APPLICATION_JSON).content("{}"))
				.andExpect(status().isBadRequest());

		verify(conversationService, never()).openConversation(any(), any(), any());
	}

	@Test
	void shouldReturn400WhenTalkingToYourself() throws Exception {
		when(conversationService.openConversation(businessId, userId, userId))
				.thenThrow(new InvalidConversationException("You cannot open a conversation with yourself"));

		mockMvc.perform(post("/conversations").with(token())
						.contentType(MediaType.APPLICATION_JSON).content("""
								{ "recipientId": "%s" }
								""".formatted(userId)))
				.andExpect(status().isBadRequest());
	}

	@Test
	void shouldListMyConversations() throws Exception {
		when(conversationService.myConversations(userId)).thenReturn(List.of(chat));

		mockMvc.perform(get("/conversations").with(token()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(1));
	}

	@Test
	void shouldSendAMessageWith201() throws Exception {
		when(conversationService.sendMessage(userId, conversationId, "Falta pollo")).thenReturn(hello);

		mockMvc.perform(post("/conversations/{conversationId}/messages", conversationId).with(token())
						.contentType(MediaType.APPLICATION_JSON).content("""
								{ "content": "Falta pollo" }
								"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.content").value("Falta pollo"))
				.andExpect(jsonPath("$.senderId").value(userId.toString()));
	}

	@Test
	void shouldReturn400WhenMessageIsTooLong() throws Exception {
		mockMvc.perform(post("/conversations/{conversationId}/messages", conversationId).with(token())
						.contentType(MediaType.APPLICATION_JSON).content("""
								{ "content": "%s" }
								""".formatted("a".repeat(2001))))
				.andExpect(status().isBadRequest());

		verify(conversationService, never()).sendMessage(any(), any(), any());
	}

	@Test
	void shouldPollMessagesAfterAnInstant() throws Exception {
		Instant after = Instant.parse("2026-11-23T10:00:30Z");
		when(conversationService.messages(userId, conversationId, after)).thenReturn(List.of(hello));

		mockMvc.perform(get("/conversations/{conversationId}/messages", conversationId)
						.param("after", after.toString()).with(token()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].content").value("Falta pollo"));
	}

	@Test
	void shouldReturn404WhenItIsNotMyConversation() throws Exception {
		when(conversationService.messages(userId, conversationId, null))
				.thenThrow(new ConversationNotFoundException("Conversation not found: " + conversationId));

		mockMvc.perform(get("/conversations/{conversationId}/messages", conversationId).with(token()))
				.andExpect(status().isNotFound());
	}

	private static User user(UUID id, String username) {
		User user = new User(username, "hash", null, username, "600000000", Role.EMPLOYEE, null);
		ReflectionTestUtils.setField(user, "id", id);   // the Conversation constructor orders by id
		return user;
	}

	private RequestPostProcessor token() {
		return jwt()
				.jwt(t -> t.subject(userId.toString()).claim("businessId", businessId.toString()).claim("role", "EMPLOYEE"))
				.authorities(new SimpleGrantedAuthority("ROLE_EMPLOYEE"));
	}
}
