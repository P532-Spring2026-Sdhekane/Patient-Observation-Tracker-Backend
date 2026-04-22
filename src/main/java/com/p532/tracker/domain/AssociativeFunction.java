package com.p532.tracker.domain;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Knowledge-level entity: a diagnostic rule.
 *
 * Week 2 additions:
 *  - strategyType: selects CONJUNCTIVE or WEIGHTED evaluation per rule
 *  - weightsJson: JSON map of phenomenonTypeId -> weight (for WEIGHTED strategy)
 *  - threshold: minimum score to fire the rule (for WEIGHTED strategy)
 */
@Entity
@Table(name = "associative_functions")
public class AssociativeFunction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String argumentConceptIds;

    @Column(nullable = false)
    private String productConcept;

    private boolean active = true;

    // ── Week 2 additions ──────────────────────────────────────────────────────

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StrategyType strategyType = StrategyType.CONJUNCTIVE;

    /**
     * JSON object mapping phenomenonTypeId (as string key) to numeric weight.
     * E.g. {"1": 0.6, "2": 0.4}
     * Only used when strategyType = WEIGHTED.
     */
    @Column(columnDefinition = "TEXT")
    private String weightsJson;

    /**
     * Score threshold for WEIGHTED strategy. Rule fires when
     * sum of present-concept weights >= threshold.
     */
    @Column
    private Double threshold = 0.5;

    public AssociativeFunction() {}

    public Long getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getArgumentConceptIds() { return argumentConceptIds; }
    public void setArgumentConceptIds(String argumentConceptIds) { this.argumentConceptIds = argumentConceptIds; }

    public List<Long> getArgumentConceptIdList() {
        if (argumentConceptIds == null || argumentConceptIds.isBlank()) return List.of();
        List<Long> ids = new ArrayList<>();
        for (String s : argumentConceptIds.split(",")) {
            try { ids.add(Long.parseLong(s.trim())); } catch (NumberFormatException ignored) {}
        }
        return ids;
    }

    public String getProductConcept() { return productConcept; }
    public void setProductConcept(String productConcept) { this.productConcept = productConcept; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public StrategyType getStrategyType() { return strategyType; }
    public void setStrategyType(StrategyType strategyType) { this.strategyType = strategyType; }

    public String getWeightsJson() { return weightsJson; }
    public void setWeightsJson(String weightsJson) { this.weightsJson = weightsJson; }

    public Double getThreshold() { return threshold; }
    public void setThreshold(Double threshold) { this.threshold = threshold; }
}
