package com.p532.tracker.observer;

import com.p532.tracker.domain.AuditLogEntry;
import com.p532.tracker.event.ObservationEvent;
import com.p532.tracker.repository.AuditLogEntryRepository;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;

@Component
public class AuditLogListener {

    private final AuditLogEntryRepository auditRepo;
    private final Clock clock;

    public AuditLogListener(AuditLogEntryRepository auditRepo, Clock clock) {
        this.auditRepo = auditRepo;
        this.clock = clock;
    }

    @EventListener
    public void onObservationEvent(ObservationEvent event) {
        String description = buildDescription(event);
        AuditLogEntry entry = new AuditLogEntry(
                description,
                event.getObservation().getId(),
                event.getObservation().getPatient().getId(),
                Instant.now(clock)
        );
        auditRepo.save(entry);
    }

    private String buildDescription(ObservationEvent event) {
        return switch (event.getEventType()) {
            case CREATED -> "Observation CREATED: type=" +
                    event.getObservation().getObservationType() +
                    " patient=" + event.getObservation().getPatient().getId();
            case REJECTED -> "Observation REJECTED: id=" +
                    event.getObservation().getId() +
                    " reason=" + event.getObservation().getRejectionReason();
        };
    }
}
