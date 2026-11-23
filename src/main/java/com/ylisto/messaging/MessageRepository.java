package com.ylisto.messaging;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

interface MessageRepository extends JpaRepository<Message, UUID> {

    List<Message> findAllByConversationIdAndSentAtAfterOrderBySentAtAsc(UUID conversationId, Instant after);
}
