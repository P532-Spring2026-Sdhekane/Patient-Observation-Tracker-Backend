package com.p532.tracker.command;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.p532.tracker.domain.Observation;
import com.p532.tracker.domain.ObservationStatus;
import com.p532.tracker.repository.ObservationRepository;

import java.util.Map;

/**
 * COMMAND PATTERN — RecordObservationCommand (Week 2 updated)
 *
 * undo(): marks the observation as REJECTED with reason "Undone by user".
 * The observation remains visible in the audit trail.
 */
public class RecordObservationCommand implements TrackerCommand {

    private final ObservationRepository observationRepo;
    private final ObjectMapper          objectMapper;
    private final Observation           observation;
    private Observation                 saved; // set after execute()

    public RecordObservationCommand(ObservationRepository observationRepo,
                                     ObjectMapper objectMapper,
                                     Observation observation) {
        this.observationRepo = observationRepo;
        this.objectMapper    = objectMapper;
        this.observation     = observation;
    }

    @Override
    public Object execute() {
        saved = observationRepo.save(observation);
        return saved;
    }

    @Override
    public Object undo() {
        Observation target = saved != null ? saved : observation;
        target.setStatus(ObservationStatus.REJECTED);
        target.setRejectionReason("Undone by user");
        return observationRepo.save(target);
    }

    @Override public String getCommandType() { return "RECORD_OBSERVATION"; }

    @Override
    public String getPayloadJson() {
        try {
            return objectMapper.writeValueAsString(Map.of(
                    "observationType", observation.getObservationType(),
                    "patientId",       observation.getPatient().getId()));
        } catch (Exception e) { return "{}"; }
    }
}
