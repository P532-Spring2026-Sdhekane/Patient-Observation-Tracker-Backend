package com.p532.tracker.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;


@Entity
@Table(name = "measurements")
@PrimaryKeyJoinColumn(name = "observation_id")
public class Measurement extends Observation {

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "phenomenon_type_id")
    private PhenomenonType phenomenonType;

    @Column(nullable = false, precision = 15, scale = 4)
    private BigDecimal amount;

    @Column(nullable = false)
    private String unit;

    public Measurement() {}

    public PhenomenonType getPhenomenonType() { return phenomenonType; }
    public void setPhenomenonType(PhenomenonType phenomenonType) { this.phenomenonType = phenomenonType; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    @Override
    public String getObservationType() { return "measurement"; }
}
