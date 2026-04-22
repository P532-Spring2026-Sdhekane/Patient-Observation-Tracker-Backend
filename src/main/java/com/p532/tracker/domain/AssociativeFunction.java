package com.p532.tracker.domain;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

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
}
