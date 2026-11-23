package com.ylisto.messaging;

import java.time.Instant;
import java.util.UUID;

public record ConversationResponse(UUID id, UUID participantOneId, UUID participantTwoId, Instant createdAt) {
    public static ConversationResponse from(Conversation conversation) {
        return new ConversationResponse(conversation.getId(), conversation.getParticipantOne().getId(),
                conversation.getParticipantTwo().getId(), conversation.getCreatedAt());
    }
}
