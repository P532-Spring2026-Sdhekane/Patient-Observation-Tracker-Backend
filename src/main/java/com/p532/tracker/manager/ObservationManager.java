package com.p532.tracker.manager;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.p532.tracker.command.CommandLog;
import com.p532.tracker.command.RecordObservationCommand;
import com.p532.tracker.command.RejectObservationCommand;
import com.p532.tracker.decorator.*;
import com.p532.tracker.domain.*;
import com.p532.tracker.engine.DiagnosisEngine;
import com.p532.tracker.event.ObservationEvent;
import com.p532.tracker.factory.ObservationFactory;
import com.p532.tracker.repository.ObservationRepository;
import com.p532.tracker.repository.PatientRepository;
import com.p532.tracker.repository.PhenomenonTypeRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.List;

/**
 * MANAGER LAYER — ObservationManager (Week 2 updated)
 *
 * Week 2 changes:
 *  1. Assembles the ObservationProcessor decorator chain:
 *       AuditStampingDecorator
 *         → AnomalyFlaggingDecorator
 *           → UnitValidationDecorator
 *             → BaseObservationProcessor
 *  2. Uses factory.createMeasurementFromRequest() / createCategoryObservationFromRequest()
 *  3. evaluateRules() now returns List<DiagnosisEngine.RuleResult> (richer response)
 *  4. Passes actingUser from the session (defaults to "staff" for Week 1 compat)
 */
@Service
public class ObservationManager {

    private final ObservationRepository   observationRepo;
    private final PatientRepository       patientRepo;
    private final PhenomenonTypeRepository phenomenonTypeRepo;
    private final ObservationFactory      factory;
    private final CommandLog              commandLog;
    private final ObjectMapper            objectMapper;
    private final ApplicationEventPublisher eventPublisher;
    private final DiagnosisEngine         diagnosisEngine;
    private final Clock                   clock;

    public ObservationManager(ObservationRepository observationRepo,
                               PatientRepository patientRepo,
                               PhenomenonTypeRepository phenomenonTypeRepo,
                               ObservationFactory factory,
                               CommandLog commandLog,
                               ObjectMapper objectMapper,
                               ApplicationEventPublisher eventPublisher,
                               DiagnosisEngine diagnosisEngine,
                               Clock clock) {
        this.observationRepo    = observationRepo;
        this.patientRepo        = patientRepo;
        this.phenomenonTypeRepo = phenomenonTypeRepo;
        this.factory            = factory;
        this.commandLog         = commandLog;
        this.objectMapper       = objectMapper;
        this.eventPublisher     = eventPublisher;
        this.diagnosisEngine    = diagnosisEngine;
        this.clock              = clock;
    }

    // ── Decorator chain assembly ──────────────────────────────────────────────

    private ObservationProcessor buildChain(String actingUser) {
        return new AuditStampingDecorator(
                new AnomalyFlaggingDecorator(
                        new UnitValidationDecorator(
                                new BaseObservationProcessor())),
                clock, actingUser);
    }

    // ── Public use-case methods ───────────────────────────────────────────────

    public List<Observation> getObservationsForPatient(Long patientId) {
        return observationRepo.findByPatientIdOrderByRecordingTimeDesc(patientId);
    }

    public Measurement recordMeasurement(Long patientId,
                                          Long phenomenonTypeId,
                                          BigDecimal amount,
                                          String unit,
                                          Instant applicabilityTime,
                                          Long protocolId,
                                          String actingUser) {
        Patient patient = patientRepo.findById(patientId)
                .orElseThrow(() -> new IllegalArgumentException("Patient not found: " + patientId));

        PhenomenonType pt = phenomenonTypeRepo.findById(phenomenonTypeId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "PhenomenonType not found: " + phenomenonTypeId));

        // Build the request and run through decorator chain
        ObservationRequest req = new ObservationRequest();
        req.setPatientId(patientId);
        req.setPhenomenonTypeId(phenomenonTypeId);
        req.setAmount(amount);
        req.setUnit(unit);
        req.setApplicabilityTime(applicabilityTime);
        req.setProtocolId(protocolId);
        req.setObservationKind("measurement");
        req.setPhenomenonType(pt);
        req.setSource(ObservationSource.MANUAL);

        ObservationRequest processed = buildChain(actingUser != null ? actingUser : "staff")
                .process(req);

        Measurement measurement = factory.createMeasurementFromRequest(patient, processed);

        Measurement saved = (Measurement) commandLog.execute(
                new RecordObservationCommand(observationRepo, objectMapper, measurement));

        eventPublisher.publishEvent(new ObservationEvent(saved, ObservationEvent.Type.CREATED));
        return saved;
    }

    /** Week 1 compatibility overload — defaults actingUser to "staff" */
    public Measurement recordMeasurement(Long patientId, Long phenomenonTypeId,
                                          BigDecimal amount, String unit,
                                          Instant applicabilityTime, Long protocolId) {
        return recordMeasurement(patientId, phenomenonTypeId, amount, unit,
                applicabilityTime, protocolId, "staff");
    }

    public CategoryObservation recordCategoryObservation(Long patientId,
                                                          Long phenomenonId,
                                                          Presence presence,
                                                          Instant applicabilityTime,
                                                          Long protocolId,
                                                          String actingUser) {
        Patient patient = patientRepo.findById(patientId)
                .orElseThrow(() -> new IllegalArgumentException("Patient not found: " + patientId));

        ObservationRequest req = new ObservationRequest();
        req.setPatientId(patientId);
        req.setPhenomenonId(phenomenonId);
        req.setPresence(presence.name());
        req.setApplicabilityTime(applicabilityTime);
        req.setProtocolId(protocolId);
        req.setObservationKind("category");
        req.setSource(ObservationSource.MANUAL);

        ObservationRequest processed = buildChain(actingUser != null ? actingUser : "staff")
                .process(req);

        CategoryObservation co = factory.createCategoryObservationFromRequest(patient, processed);

        CategoryObservation saved = (CategoryObservation) commandLog.execute(
                new RecordObservationCommand(observationRepo, objectMapper, co));

        eventPublisher.publishEvent(new ObservationEvent(saved, ObservationEvent.Type.CREATED));
        return saved;
    }

    public CategoryObservation recordCategoryObservation(Long patientId, Long phenomenonId,
                                                          Presence presence,
                                                          Instant applicabilityTime,
                                                          Long protocolId) {
        return recordCategoryObservation(patientId, phenomenonId, presence,
                applicabilityTime, protocolId, "staff");
    }

    public Observation rejectObservation(Long observationId, String reason) {
        Observation observation = observationRepo.findById(observationId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Observation not found: " + observationId));

        if (observation.getStatus() == ObservationStatus.REJECTED) {
            throw new IllegalStateException("Observation is already rejected");
        }

        Observation saved = (Observation) commandLog.execute(
                new RejectObservationCommand(observationRepo, objectMapper, observation, reason));

        eventPublisher.publishEvent(new ObservationEvent(saved, ObservationEvent.Type.REJECTED));
        return saved;
    }

    public List<DiagnosisEngine.RuleResult> evaluateRules(Long patientId) {
        if (!patientRepo.existsById(patientId)) {
            throw new IllegalArgumentException("Patient not found: " + patientId);
        }
        return diagnosisEngine.evaluateRules(patientId);
    }

    /** Used by PropagationListener (Change 4) to save inferred observations directly */
    public Observation saveInferredObservation(Observation obs) {
        Observation saved = observationRepo.save(obs);
        eventPublisher.publishEvent(new ObservationEvent(saved, ObservationEvent.Type.CREATED));
        return saved;
    }
}
