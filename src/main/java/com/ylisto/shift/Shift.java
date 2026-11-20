package com.ylisto.shift;

import com.ylisto.user.User;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

import static jakarta.persistence.FetchType.LAZY;

@Entity
public class Shift {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false)
    private UUID id;
    @ManyToOne(fetch = LAZY,optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    @Column(nullable=false)
    private Instant startsAt;
    @Column(nullable=false)
    private Instant endsAt;

    public Shift(User user, Instant startsAt, Instant endsAt) {
        this.user = user;
        this.startsAt = startsAt;
        this.endsAt = endsAt;
    }

    protected Shift() {
    }

    public UUID getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public Instant getStartsAt() {
        return startsAt;
    }

    public Instant getEndsAt() {
        return endsAt;
    }
}
