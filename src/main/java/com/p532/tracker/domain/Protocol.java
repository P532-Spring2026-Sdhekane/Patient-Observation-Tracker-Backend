package com.p532.tracker.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "protocols")
public class Protocol {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccuracyRating accuracyRating;

    public Protocol() {}

    public Protocol(String name, String description, AccuracyRating accuracyRating) {
        this.name = name;
        this.description = description;
        this.accuracyRating = accuracyRating;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public AccuracyRating getAccuracyRating() { return accuracyRating; }
    public void setAccuracyRating(AccuracyRating accuracyRating) { this.accuracyRating = accuracyRating; }
}
