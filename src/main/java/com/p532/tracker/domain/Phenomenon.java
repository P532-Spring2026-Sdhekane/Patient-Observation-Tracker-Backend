package com.p532.tracker.domain;

import jakarta.persistence.*;

/**
 * Knowledge-level entity: a specific qualitative value belonging to a PhenomenonType.
 * E.g. "Blood Group A" belongs to "Blood Group" PhenomenonType.
 */
@Entity
@Table(name = "phenomena")
public class Phenomenon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "phenomenon_type_id", nullable = false)
    private PhenomenonType phenomenonType;

    public Phenomenon() {}

    public Phenomenon(String name, PhenomenonType phenomenonType) {
        this.name = name;
        this.phenomenonType = phenomenonType;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public PhenomenonType getPhenomenonType() { return phenomenonType; }
    public void setPhenomenonType(PhenomenonType phenomenonType) { this.phenomenonType = phenomenonType; }
}
