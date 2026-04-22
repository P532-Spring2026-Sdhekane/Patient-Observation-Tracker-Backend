package com.p532.tracker.decorator;

import com.p532.tracker.domain.ObservationSource;
import com.p532.tracker.domain.PhenomenonType;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * DECORATOR PATTERN — ObservationRequest
 *
 * Mutable DTO that travels through the decorator chain before
 * an observation is handed to the factory and persisted.
 *
 * Each decorator reads and/or enriches this object:
 *  - AuditStampingDecorator   → sets recordingTime, actingUser
 *  - UnitValidationDecorator  → validates unit against allowedUnits
 *  - AnomalyFlaggingDecorator → sets anomalyFlag if value is out of range
 */
public class ObservationRequest {

    // ── Input fields (set by ObservationManager before chain) ────────────────
    private Long patientId;
    private Long phenomenonTypeId;      // for measurements
    private Long phenomenonId;          // for category observations
    private BigDecimal amount;          // for measurements; null for category
    private String unit;                // for measurements; null for category
    private String presence;            // for category: "PRESENT" or "ABSENT"
    private Instant applicabilityTime;  // optional; defaults to now
    private Long protocolId;            // optional
    private String observationKind;     // "measurement" or "category"

    // ── Enriched by decorators ────────────────────────────────────────────────
    private Instant recordingTime;
    private String actingUser;
    private boolean anomalyFlag   = false;
    private ObservationSource source = ObservationSource.MANUAL;

    // ── PhenomenonType cached for decorator use ───────────────────────────────
    private PhenomenonType phenomenonType;

    public ObservationRequest() {}

    // Getters & setters
    public Long getPatientId()                  { return patientId; }
    public void setPatientId(Long v)            { this.patientId = v; }

    public Long getPhenomenonTypeId()           { return phenomenonTypeId; }
    public void setPhenomenonTypeId(Long v)     { this.phenomenonTypeId = v; }

    public Long getPhenomenonId()               { return phenomenonId; }
    public void setPhenomenonId(Long v)         { this.phenomenonId = v; }

    public BigDecimal getAmount()               { return amount; }
    public void setAmount(BigDecimal v)         { this.amount = v; }

    public String getUnit()                     { return unit; }
    public void setUnit(String v)               { this.unit = v; }

    public String getPresence()                 { return presence; }
    public void setPresence(String v)           { this.presence = v; }

    public Instant getApplicabilityTime()       { return applicabilityTime; }
    public void setApplicabilityTime(Instant v) { this.applicabilityTime = v; }

    public Long getProtocolId()                 { return protocolId; }
    public void setProtocolId(Long v)           { this.protocolId = v; }

    public String getObservationKind()          { return observationKind; }
    public void setObservationKind(String v)    { this.observationKind = v; }

    public Instant getRecordingTime()           { return recordingTime; }
    public void setRecordingTime(Instant v)     { this.recordingTime = v; }

    public String getActingUser()               { return actingUser; }
    public void setActingUser(String v)         { this.actingUser = v; }

    public boolean isAnomalyFlag()              { return anomalyFlag; }
    public void setAnomalyFlag(boolean v)       { this.anomalyFlag = v; }

    public ObservationSource getSource()        { return source; }
    public void setSource(ObservationSource v)  { this.source = v; }

    public PhenomenonType getPhenomenonType()   { return phenomenonType; }
    public void setPhenomenonType(PhenomenonType v) { this.phenomenonType = v; }
}
