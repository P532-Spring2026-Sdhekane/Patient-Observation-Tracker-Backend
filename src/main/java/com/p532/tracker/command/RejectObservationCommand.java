package com.p532.tracker.command;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.p532.tracker.domain.Observation;
import com.p532.tracker.domain.ObservationStatus;
import com.p532.tracker.repository.ObservationRepository;

import java.util.Map;

/**
 * COMMAND PATTERN — RejectObservationCommand (Week 2 updated)
 *
 * undo(): restores the observation status to ACTIVE and clears the
 * rejection reason, effectively un-rejecting it.
 */
public class RejectObservationCommand implements TrackerCommand {

    private final ObservationRepository observationRepo;
    private final ObjectMapper          objectMapper;
    private final Observation           observation;
    private final String                reason;

    public RejectObservationCommand(ObservationRepository observationRepo,
                                     ObjectMapper objectMapper,
                                     Observation observation,
                                     String reason) {
        this.observationRepo = observationRepo;
        this.objectMapper    = objectMapper;
        this.observation     = observation;
        this.reason          = reason;
    }

    @Override
    public Object execute() {
        observation.setStatus(ObservationStatus.REJECTED);
        observation.setRejectionReason(reason);
        return observationRepo.save(observation);
    }

    @Override
    public Object undo() {
        observation.setStatus(ObservationStatus.ACTIVE);
        observation.setRejectionReason(null);
        return observationRepo.save(observation);
    }

    @Override public String getCommandType() { return "REJECT_OBSERVATION"; }

    @Override
    public String getPayloadJson() {
        try {
            return objectMapper.writeValueAsString(Map.of(
                    "observationId", observation.getId(),
                    "patientId",     observation.getPatient().getId(),
                    "reason",        reason != null ? reason : ""));
        } catch (Exception e) { return "{}"; }
    }
}
