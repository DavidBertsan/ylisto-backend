package com.ylisto.inventory;

import com.ylisto.user.User;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

import static jakarta.persistence.FetchType.LAZY;


@Entity
public class StockMovement {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @ManyToOne(optional = false, fetch = LAZY)
    @JoinColumn(name = "variant_id",nullable = false)
    private ProductVariant variant;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MovementType type;
    @Column(nullable = false, precision = 10, scale = 3)
    private BigDecimal quantityChange;
    @ManyToOne(optional = false, fetch = LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    @Column(nullable = false)
    private Instant occurredAt;


    public StockMovement(ProductVariant variant, MovementType type, BigDecimal quantityChange, User user, Instant occurredAt) {
        this.variant = variant;
        this.type = type;
        this.quantityChange = quantityChange;
        this.user = user;
        this.occurredAt = occurredAt;

    }

    protected StockMovement() {
    }



    public UUID getId() {
        return id;
    }

    public ProductVariant getVariant() {
        return variant;
    }

    public MovementType getType() {
        return type;
    }

    public BigDecimal getQuantityChange() {
        return quantityChange;
    }

    public User getUser() {
        return user;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }
}
