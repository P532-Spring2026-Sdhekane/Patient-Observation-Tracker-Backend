package com.p532.tracker.domain;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "phenomenon_types")
public class PhenomenonType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MeasurementKind kind;

    @Column(columnDefinition = "TEXT")
    private String allowedUnitsRaw;

    @OneToMany(mappedBy = "phenomenonType", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<Phenomenon> phenomena = new ArrayList<>();

    public PhenomenonType() {}

    public PhenomenonType(String name, MeasurementKind kind, String allowedUnitsRaw) {
        this.name = name;
        this.kind = kind;
        this.allowedUnitsRaw = allowedUnitsRaw;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public MeasurementKind getKind() { return kind; }
    public void setKind(MeasurementKind kind) { this.kind = kind; }
    public String getAllowedUnitsRaw() { return allowedUnitsRaw; }
    public void setAllowedUnitsRaw(String allowedUnitsRaw) { this.allowedUnitsRaw = allowedUnitsRaw; }
    public List<Phenomenon> getPhenomena() { return phenomena; }

    public List<String> getAllowedUnits() {
        if (allowedUnitsRaw == null || allowedUnitsRaw.isBlank()) return List.of();
        return List.of(allowedUnitsRaw.split(","));
    }
}
