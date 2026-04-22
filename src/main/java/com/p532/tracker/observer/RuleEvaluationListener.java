package com.p532.tracker.observer;

import com.p532.tracker.engine.DiagnosisEngine;
import com.p532.tracker.event.ObservationEvent;
import com.p532.tracker.repository.ObservationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RuleEvaluationListener {

    private static final Logger log = LoggerFactory.getLogger(RuleEvaluationListener.class);

    private final DiagnosisEngine diagnosisEngine;
    private final ObservationRepository observationRepo;

    public RuleEvaluationListener(DiagnosisEngine diagnosisEngine,
                                   ObservationRepository observationRepo) {
        this.diagnosisEngine = diagnosisEngine;
        this.observationRepo = observationRepo;
    }

    @EventListener
    public void onObservationEvent(ObservationEvent event) {
        Long patientId = event.getObservation().getPatient().getId();
        List<String> inferences = diagnosisEngine.evaluateRules(patientId);
        if (!inferences.isEmpty()) {
            log.info("Rule evaluation after {} event for patient {}: inferred concepts = {}",
                    event.getEventType(), patientId, inferences);
        }
    }
}
