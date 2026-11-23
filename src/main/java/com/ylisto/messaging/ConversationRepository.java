package com.ylisto.messaging;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

interface ConversationRepository extends JpaRepository<Conversation, UUID> {
    Optional<Conversation> findByParticipantOneIdAndParticipantTwoId(UUID participantOneId, UUID participantTwoId);
    List<Conversation> findAllByParticipantOneIdOrParticipantTwoIdOrderByCreatedAtDesc(UUID userId, UUID sameUserId);

}
