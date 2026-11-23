package com.ylisto.messaging;

import com.ylisto.user.User;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name="id",updatable = false)
    private UUID id;
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id", nullable = false)
    private Conversation conversation;
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;
    @Column(nullable = false, length = 2000)
    private String content;
    @Column(nullable = false)
    private Instant sentAt;

    public Message(Conversation conversation, User sender, String content, Instant sentAt) {
        this.conversation = conversation;
        this.sender = sender;
        this.content = content;
        this.sentAt = sentAt;
    }

    protected Message() {
    }

    public UUID getId() {
        return id;
    }

    public Conversation getConversation() {
        return conversation;
    }

    public User getSender() {
        return sender;
    }

    public String getContent() {
        return content;
    }

    public Instant getSentAt() {
        return sentAt;
    }
}
