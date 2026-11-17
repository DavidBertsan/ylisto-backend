package com.ylisto.business;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
public class Business {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name="id",updatable = false)
    private UUID id;
    @Column(nullable = false)
    private String name;
    private String type;

    public Business(String name, String type) {
        this.name = name;
        this.type = type;
    }

    protected Business() {
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }
}
