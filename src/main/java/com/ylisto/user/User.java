package com.ylisto.user;

import com.ylisto.business.Business;
import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name="id",updatable = false)
    private UUID id;
    @Column(nullable = false, unique = true)
    private String username;
    @Column(nullable = false )
    private String password;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "business_id", nullable = false)
    private Business business;
    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    private String mobile;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;
    private String imageUrl;

    public User(String username, String password, Business business, String name, String mobile, Role role, String imageUrl) {
        this.username = username;
        this.password = password;
        this.business = business;
        this.name = name;
        this.mobile = mobile;
        this.role = role;
        this.imageUrl = imageUrl;
    }

    protected User() {
    }

    public UUID getId() {
        return id;
    }

    public Business getBusiness() {
        return business;
    }

    public String getPassword() {
        return password;
    }

    public String getUsername() {
        return username;
    }

    public String getName() {
        return name;
    }

    public String getMobile() {
        return mobile;
    }

    public Role getRole() {
        return role;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}
