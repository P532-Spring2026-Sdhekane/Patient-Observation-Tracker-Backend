package com.p532.tracker.command;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.p532.tracker.domain.Observation;
import com.p532.tracker.repository.ObservationRepository;

import java.util.Map;


public class RecordObservationCommand implements TrackerCommand {

    private final ObservationRepository observationRepo;
    private final ObjectMapper objectMapper;
    private final Observation observation;

    public RecordObservationCommand(ObservationRepository observationRepo,
                                     ObjectMapper objectMapper,
                                     Observation observation) {
        this.observationRepo = observationRepo;
        this.objectMapper = objectMapper;
        this.observation = observation;
    }

    @Override
    public Object execute() {
        return observationRepo.save(observation);
    }

    @Override
    public String getCommandType() {
        return "RECORD_OBSERVATION";
    }

    @Override
    public String getPayloadJson() {
        try {
            return objectMapper.writeValueAsString(Map.of(
                    "observationType", observation.getObservationType(),
                    "patientId", observation.getPatient().getId()
            ));
        } catch (Exception e) {
            return "{}";
        }
    }
}
