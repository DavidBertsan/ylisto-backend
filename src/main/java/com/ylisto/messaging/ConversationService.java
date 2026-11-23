package com.ylisto.messaging;

import com.ylisto.user.User;
import com.ylisto.user.UserNotFoundException;
import com.ylisto.user.UserService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class ConversationService {
    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final UserService userService;
    private final Clock clock;

    public ConversationService(ConversationRepository conversationRepository, MessageRepository messageRepository, UserService userService, Clock clock) {
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.userService = userService;
        this.clock =clock;
    }
    @Transactional
    public Conversation openConversation(UUID businessId, UUID userId, UUID recipientId) {
        if (recipientId.equals(userId)) {
            throw new InvalidConversationException("You cannot open a conversation with yourself");
        }
        User recipient = userService.findByIdAndBusinessId(recipientId, businessId)
                .filter(User::isActive)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + recipientId));
        User me = userService.findById(userId);

        boolean meFirst = userId.compareTo(recipientId) < 0;
        UUID firstId = meFirst ? userId : recipientId;
        UUID secondId = meFirst ? recipientId : userId;

        return conversationRepository.findByParticipantOneIdAndParticipantTwoId(firstId, secondId)
                .orElseGet(() -> conversationRepository.save(
                        new Conversation(me.getBusiness(), me, recipient, Instant.now(clock))));
    }

    public List<Conversation> myConversations(UUID userId) {
        return conversationRepository.findAllByParticipantOneIdOrParticipantTwoIdOrderByCreatedAtDesc(userId, userId);
    }

    @Transactional
    public Message sendMessage(UUID userId, UUID conversationId, String content){
        Conversation conversation = findMyConversation(userId, conversationId);
        User sender=userService.findById(userId);

        return messageRepository.save(new Message(conversation,sender,content,clock.instant()));
    }
    public List<Message> messages(UUID userId, UUID conversationId, Instant after){
        findMyConversation(userId, conversationId);

        if(after==null){
            after=Instant.EPOCH;
        }
      return  messageRepository.findAllByConversationIdAndSentAtAfterOrderBySentAtAsc(conversationId,after);
    }

    private Conversation findMyConversation(UUID userId, UUID conversationId) {
        return conversationRepository.findById(conversationId)
                .filter(c -> c.hasParticipant(userId))
                .orElseThrow(() -> new ConversationNotFoundException("Conversation not found: " + conversationId));
    }
}
