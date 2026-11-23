package com.ylisto.messaging;

import java.time.Instant;
import java.util.UUID;

public record MessageResponse(UUID id, UUID conversationId, UUID senderId, String content, Instant sentAt) {
    public static MessageResponse from(Message message) {
        return new MessageResponse(message.getId(), message.getConversation().getId(),
                message.getSender().getId(), message.getContent(), message.getSentAt());
    }
}
