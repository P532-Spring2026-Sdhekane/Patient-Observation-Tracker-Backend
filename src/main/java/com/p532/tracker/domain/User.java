package com.p532.tracker.domain;

import jakarta.persistence.*;

/**
 * Week 2 — Change 3: replaces the hard-coded "staff" user.
 * No full auth required; the UI shows a login dropdown that sets
 * the current username, which is passed to CommandLog.
 */
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    public User() {}

    public User(String username, UserRole role) {
        this.username = username;
        this.role     = role;
    }

    public Long getId()             { return id; }
    public String getUsername()     { return username; }
    public void setUsername(String u) { this.username = u; }
    public UserRole getRole()       { return role; }
    public void setRole(UserRole r) { this.role = r; }
}
