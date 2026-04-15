package com.p532.tracker.domain;

import jakarta.persistence.*;
import java.time.Instant;


@Entity
@Table(name = "audit_log_entries")
public class AuditLogEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String event;

    private Long observationId;
    private Long patientId;

    @Column(nullable = false)
    private Instant timestamp;

    public AuditLogEntry() {}

    public AuditLogEntry(String event, Long observationId, Long patientId, Instant timestamp) {
        this.event = event;
        this.observationId = observationId;
        this.patientId = patientId;
        this.timestamp = timestamp;
    }

    public Long getId() { return id; }
    public String getEvent() { return event; }
    public Long getObservationId() { return observationId; }
    public Long getPatientId() { return patientId; }
    public Instant getTimestamp() { return timestamp; }
}
