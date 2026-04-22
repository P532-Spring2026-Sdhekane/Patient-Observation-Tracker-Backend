package com.p532.tracker.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Knowledge-level entity: describes a category of observable phenomena.
 *
 * Week 2 addition:
 *  - normalMin / normalMax: configurable normal range used by AnomalyFlaggingDecorator.
 *    If a measurement value falls outside [normalMin, normalMax], the observation
 *    is flagged as anomalous. Null means no range check for that bound.
 */
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

    @OneToMany(mappedBy = "phenomenonType", cascade = CascadeType.ALL,
               orphanRemoval = true, fetch = FetchType.EAGER)
    private List<Phenomenon> phenomena = new ArrayList<>();

    // ── Week 2 additions ──────────────────────────────────────────────────────
    @Column
    private Double normalMin;

    @Column
    private Double normalMax;

    public PhenomenonType() {}

    public PhenomenonType(String name, MeasurementKind kind, String allowedUnitsRaw) {
        this.name           = name;
        this.kind           = kind;
        this.allowedUnitsRaw = allowedUnitsRaw;
    }

    public Long getId()             { return id; }
    public String getName()         { return name; }
    public void setName(String n)   { this.name = n; }
    public MeasurementKind getKind(){ return kind; }
    public void setKind(MeasurementKind k) { this.kind = k; }
    public String getAllowedUnitsRaw()        { return allowedUnitsRaw; }
    public void setAllowedUnitsRaw(String u) { this.allowedUnitsRaw = u; }

    @JsonIgnore
    public List<Phenomenon> getPhenomena() { return phenomena; }

    public List<String> getAllowedUnits() {
        if (allowedUnitsRaw == null || allowedUnitsRaw.isBlank()) return List.of();
        return List.of(allowedUnitsRaw.split(","));
    }

    public Double getNormalMin()          { return normalMin; }
    public void setNormalMin(Double v)    { this.normalMin = v; }
    public Double getNormalMax()          { return normalMax; }
    public void setNormalMax(Double v)    { this.normalMax = v; }
}
