package com.ylisto.clocking;


import com.ylisto.user.User;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

import static jakarta.persistence.FetchType.LAZY;

@Entity
public class Clocking {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name="id",updatable = false)
    private UUID id;
    @Column(nullable = false)
    private Instant clockIn;
    private Instant clockOut;
    @ManyToOne(optional = false, fetch = LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public Clocking(User user, Instant clockIn) {
        this.user = user;
        this.clockIn = clockIn;


    }

    protected Clocking() {
    }

    public UUID getId() {
        return id;
    }

    public Instant getClockIn() {
        return clockIn;
    }

    public Instant getClockOut() {
        return clockOut;
    }

    public User getUser() {
        return user;
    }

    public void clockOut(Instant time) {
        this.clockOut = time;
    }
}
