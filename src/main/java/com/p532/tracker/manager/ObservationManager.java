package com.p532.tracker.manager;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.p532.tracker.command.CommandLog;
import com.p532.tracker.command.RecordObservationCommand;
import com.p532.tracker.command.RejectObservationCommand;
import com.p532.tracker.domain.*;
import com.p532.tracker.engine.DiagnosisEngine;
import com.p532.tracker.event.ObservationEvent;
import com.p532.tracker.factory.ObservationFactory;
import com.p532.tracker.repository.ObservationRepository;
import com.p532.tracker.repository.PatientRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;


@Service
public class ObservationManager {

    private final ObservationRepository observationRepo;
    private final PatientRepository patientRepo;
    private final ObservationFactory factory;
    private final CommandLog commandLog;
    private final ObjectMapper objectMapper;
    private final ApplicationEventPublisher eventPublisher;
    private final DiagnosisEngine diagnosisEngine;

    public ObservationManager(ObservationRepository observationRepo,
                               PatientRepository patientRepo,
                               ObservationFactory factory,
                               CommandLog commandLog,
                               ObjectMapper objectMapper,
                               ApplicationEventPublisher eventPublisher,
                               DiagnosisEngine diagnosisEngine) {
        this.observationRepo = observationRepo;
        this.patientRepo = patientRepo;
        this.factory = factory;
        this.commandLog = commandLog;
        this.objectMapper = objectMapper;
        this.eventPublisher = eventPublisher;
        this.diagnosisEngine = diagnosisEngine;
    }

    public List<Observation> getObservationsForPatient(Long patientId) {
        return observationRepo.findByPatientIdOrderByRecordingTimeDesc(patientId);
    }

    public Measurement recordMeasurement(Long patientId,
                                          Long phenomenonTypeId,
                                          BigDecimal amount,
                                          String unit,
                                          Instant applicabilityTime,
                                          Long protocolId) {
        Patient patient = patientRepo.findById(patientId)
                .orElseThrow(() -> new IllegalArgumentException("Patient not found: " + patientId));

        // Factory validates all constraints — manager trusts the result
        Measurement measurement = factory.createMeasurement(
                patient, phenomenonTypeId, amount, unit, applicabilityTime, protocolId);

        Measurement saved = (Measurement) commandLog.execute(
                new RecordObservationCommand(observationRepo, objectMapper, measurement));

        eventPublisher.publishEvent(new ObservationEvent(saved, ObservationEvent.Type.CREATED));
        return saved;
    }

    public CategoryObservation recordCategoryObservation(Long patientId,
                                                          Long phenomenonId,
                                                          Presence presence,
                                                          Instant applicabilityTime,
                                                          Long protocolId) {
        Patient patient = patientRepo.findById(patientId)
                .orElseThrow(() -> new IllegalArgumentException("Patient not found: " + patientId));

        CategoryObservation co = factory.createCategoryObservation(
                patient, phenomenonId, presence, applicabilityTime, protocolId);

        CategoryObservation saved = (CategoryObservation) commandLog.execute(
                new RecordObservationCommand(observationRepo, objectMapper, co));

        eventPublisher.publishEvent(new ObservationEvent(saved, ObservationEvent.Type.CREATED));
        return saved;
    }

    public Observation rejectObservation(Long observationId, String reason) {
        Observation observation = observationRepo.findById(observationId)
                .orElseThrow(() -> new IllegalArgumentException("Observation not found: " + observationId));

        if (observation.getStatus() == ObservationStatus.REJECTED) {
            throw new IllegalStateException("Observation is already rejected");
        }

        Observation saved = (Observation) commandLog.execute(
                new RejectObservationCommand(observationRepo, objectMapper, observation, reason));

        eventPublisher.publishEvent(new ObservationEvent(saved, ObservationEvent.Type.REJECTED));
        return saved;
    }

    public List<String> evaluateRules(Long patientId) {
        if (!patientRepo.existsById(patientId)) {
            throw new IllegalArgumentException("Patient not found: " + patientId);
        }
        return diagnosisEngine.evaluateRules(patientId);
    }
}
