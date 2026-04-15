package com.p532.tracker;

import com.p532.tracker.domain.*;
import com.p532.tracker.engine.DiagnosisEngine;
import com.p532.tracker.event.ObservationEvent;
import com.p532.tracker.observer.AuditLogListener;
import com.p532.tracker.observer.RuleEvaluationListener;
import com.p532.tracker.repository.AuditLogEntryRepository;
import com.p532.tracker.repository.ObservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ObserverListenerTest {

    @Mock private AuditLogEntryRepository auditRepo;
    @Mock private DiagnosisEngine diagnosisEngine;
    @Mock private ObservationRepository observationRepo;

    private AuditLogListener auditLogListener;
    private RuleEvaluationListener ruleEvaluationListener;

    private final Clock fixedClock = Clock.fixed(Instant.parse("2025-06-01T09:00:00Z"), ZoneOffset.UTC);

    @BeforeEach
    void setUp() {
        auditLogListener = new AuditLogListener(auditRepo, fixedClock);
        ruleEvaluationListener = new RuleEvaluationListener(diagnosisEngine, observationRepo);
    }

    private Measurement buildMeasurement(Long patientId) {
        Patient patient = new Patient("Test", null, null);
        PhenomenonType pt = new PhenomenonType("Temp", MeasurementKind.QUANTITATIVE, "°C");
        Measurement m = new Measurement();
        m.setPatient(patient);
        m.setPhenomenonType(pt);
        m.setStatus(ObservationStatus.ACTIVE);
        m.setRecordingTime(Instant.now());
        m.setApplicabilityTime(Instant.now());
        return m;
    }

    @Test
    void auditLogListener_onCreatedEvent_savesAuditEntryWithCreatedDescription() {
        // Arrange
        Measurement m = buildMeasurement(1L);
        ObservationEvent event = new ObservationEvent(m, ObservationEvent.Type.CREATED);
        when(auditRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // Act
        auditLogListener.onObservationEvent(event);

        // Assert
        ArgumentCaptor<AuditLogEntry> captor = ArgumentCaptor.forClass(AuditLogEntry.class);
        verify(auditRepo).save(captor.capture());
        assertTrue(captor.getValue().getEvent().contains("CREATED"));
    }

    @Test
    void auditLogListener_onRejectedEvent_savesAuditEntryWithRejectedDescription() {
        // Arrange
        Measurement m = buildMeasurement(2L);
        m.setStatus(ObservationStatus.REJECTED);
        m.setRejectionReason("Incorrect reading");
        ObservationEvent event = new ObservationEvent(m, ObservationEvent.Type.REJECTED);
        when(auditRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // Act
        auditLogListener.onObservationEvent(event);

        // Assert
        ArgumentCaptor<AuditLogEntry> captor = ArgumentCaptor.forClass(AuditLogEntry.class);
        verify(auditRepo).save(captor.capture());
        assertTrue(captor.getValue().getEvent().contains("REJECTED"));
    }

    @Test
    void ruleEvaluationListener_onEvent_callsDiagnosisEngineForPatient() {
        // Arrange
        Measurement m = buildMeasurement(3L);
        ObservationEvent event = new ObservationEvent(m, ObservationEvent.Type.CREATED);
        when(diagnosisEngine.evaluateRules(any())).thenReturn(List.of());

        // Act
        ruleEvaluationListener.onObservationEvent(event);

        // Assert
        verify(diagnosisEngine, times(1)).evaluateRules(m.getPatient().getId());
    }

    @Test
    void auditLogListener_onCreatedEvent_timestampMatchesFixedClock() {
        // Arrange
        Measurement m = buildMeasurement(4L);
        ObservationEvent event = new ObservationEvent(m, ObservationEvent.Type.CREATED);
        when(auditRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // Act
        auditLogListener.onObservationEvent(event);

        // Assert
        ArgumentCaptor<AuditLogEntry> captor = ArgumentCaptor.forClass(AuditLogEntry.class);
        verify(auditRepo).save(captor.capture());
        assertEquals(Instant.parse("2025-06-01T09:00:00Z"), captor.getValue().getTimestamp());
    }
}
