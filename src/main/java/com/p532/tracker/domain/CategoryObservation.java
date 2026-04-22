package com.p532.tracker.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "category_observations")
@PrimaryKeyJoinColumn(name = "observation_id")
public class CategoryObservation extends Observation {

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "phenomenon_id")
    private Phenomenon phenomenon;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Presence presence;

    public CategoryObservation() {}

    public Phenomenon getPhenomenon() { return phenomenon; }
    public void setPhenomenon(Phenomenon phenomenon) { this.phenomenon = phenomenon; }

    public Presence getPresence() { return presence; }
    public void setPresence(Presence presence) { this.presence = presence; }

    @Override
    public String getObservationType() { return "category"; }
}
