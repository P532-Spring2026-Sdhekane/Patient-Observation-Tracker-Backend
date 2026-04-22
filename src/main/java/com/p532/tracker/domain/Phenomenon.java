package com.p532.tracker.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Knowledge-level entity: a specific qualitative value belonging to a PhenomenonType.
 *
 * Week 2 addition (Change 4):
 *  - parentConcept: nullable self-reference enabling concept hierarchy.
 *    E.g. "Fever" can be a child of "Elevated Temperature".
 *
 * Propagation rules (enforced by PropagationListener):
 *  - PRESENT on a child → infer PRESENT on all ancestors
 *  - ABSENT  on a parent → infer ABSENT on all descendants
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

    // ── Week 2: concept hierarchy ─────────────────────────────────────────────

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_concept_id")
    private Phenomenon parentConcept;

    @OneToMany(mappedBy = "parentConcept", fetch = FetchType.LAZY)
    private List<Phenomenon> childConcepts = new ArrayList<>();

    public Phenomenon() {}

    public Phenomenon(String name, PhenomenonType phenomenonType) {
        this.name           = name;
        this.phenomenonType = phenomenonType;
    }

    public Long getId()     { return id; }
    public String getName() { return name; }
    public void setName(String n) { this.name = n; }

    @JsonIgnore
    public PhenomenonType getPhenomenonType() { return phenomenonType; }
    public void setPhenomenonType(PhenomenonType pt) { this.phenomenonType = pt; }

    // Expose just the id for JSON so the frontend can display it without circular refs
    public Long getPhenomenonTypeId() {
        return phenomenonType != null ? phenomenonType.getId() : null;
    }
    public String getPhenomenonTypeName() {
        return phenomenonType != null ? phenomenonType.getName() : null;
    }

    @JsonIgnore
    public Phenomenon getParentConcept() { return parentConcept; }
    public void setParentConcept(Phenomenon parent) { this.parentConcept = parent; }

    public Long getParentConceptId() {
        return parentConcept != null ? parentConcept.getId() : null;
    }

    @JsonIgnore
    public List<Phenomenon> getChildConcepts() { return childConcepts; }
}
