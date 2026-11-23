package com.ylisto.messaging;


import com.ylisto.business.Business;
import com.ylisto.user.User;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

import static jakarta.persistence.FetchType.LAZY;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"participant_one_id", "participant_two_id"}))
public class Conversation {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name="id",updatable = false)
    private UUID id;
    @ManyToOne(optional = false, fetch = LAZY)
    @JoinColumn(name = "business_id", nullable = false)
    private Business business;
    @ManyToOne(optional = false, fetch = LAZY)
    @JoinColumn(name = "participant_one_id", nullable = false)
    private User participantOne;
    @ManyToOne(optional = false, fetch = LAZY)
    @JoinColumn(name = "participant_two_id", nullable = false)
    private User participantTwo;
    @Column(nullable = false)
    private Instant createdAt;

    public Conversation(Business business, User a, User b, Instant createdAt) {
        this.business = business;
        if (a.getId().compareTo(b.getId()) < 0) {
            this.participantOne = a;
            this.participantTwo = b;
        } else {
            this.participantOne = b;
            this.participantTwo = a;
        }

        this.createdAt = createdAt;
    }

    protected Conversation() {
    }
    public boolean hasParticipant(UUID userId) {
        return participantOne.getId().equals(userId) || participantTwo.getId().equals(userId);
    }

    public UUID getId() {
        return id;
    }

    public Business getBusiness() {
        return business;
    }

    public User getParticipantOne() {
        return participantOne;
    }

    public User getParticipantTwo() {
        return participantTwo;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

}
