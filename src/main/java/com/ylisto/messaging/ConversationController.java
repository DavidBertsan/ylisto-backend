package com.ylisto.messaging;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/conversations")
public class ConversationController {
    private final ConversationService conversationService;

    public ConversationController(ConversationService conversationService) {
        this.conversationService = conversationService;
    }

    // 200, not 201: it may return a conversation that already existed
    @PostMapping
    public ResponseEntity<ConversationResponse> openConversation(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody OpenConversationRequest request) {
        UUID userId = UUID.fromString(jwt.getSubject());
        UUID businessId = UUID.fromString(jwt.getClaimAsString("businessId"));

        return ResponseEntity.ok(ConversationResponse.from(
                conversationService.openConversation(businessId, userId, request.recipientId())));
    }

    @GetMapping
    public ResponseEntity<List<ConversationResponse>> myConversations(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());

        return ResponseEntity.ok(conversationService.myConversations(userId).stream().map(ConversationResponse::from).toList());
    }

    @PostMapping("/{conversationId}/messages")
    public ResponseEntity<MessageResponse> sendMessage(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID conversationId,
                                                       @Valid @RequestBody SendMessageRequest request) {
        UUID userId = UUID.fromString(jwt.getSubject());

        return ResponseEntity.status(HttpStatus.CREATED).body(MessageResponse.from(
                conversationService.sendMessage(userId, conversationId, request.content())));
    }

    @GetMapping("/{conversationId}/messages")
    public ResponseEntity<List<MessageResponse>> messages(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID conversationId,
                                                          @RequestParam(required = false) Instant after) {
        UUID userId = UUID.fromString(jwt.getSubject());

        return ResponseEntity.ok(conversationService.messages(userId, conversationId, after).stream().map(MessageResponse::from).toList());
    }
}
