package com.p532.tracker.observer;

import com.p532.tracker.engine.DiagnosisEngine;
import com.p532.tracker.event.ObservationEvent;
import com.p532.tracker.repository.ObservationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * OBSERVER PATTERN — RuleEvaluationListener (Week 2 updated)
 *
 * Re-evaluates all active diagnostic rules for the affected patient
 * after any observation lifecycle event. Logs each inferred concept
 * together with the strategy used (new in Week 2).
 */
@Component
public class RuleEvaluationListener {

    private static final Logger log = LoggerFactory.getLogger(RuleEvaluationListener.class);

    private final DiagnosisEngine    diagnosisEngine;
    private final ObservationRepository observationRepo;

    public RuleEvaluationListener(DiagnosisEngine diagnosisEngine,
                                   ObservationRepository observationRepo) {
        this.diagnosisEngine  = diagnosisEngine;
        this.observationRepo  = observationRepo;
    }

    @EventListener
    public void onObservationEvent(ObservationEvent event) {
        Long patientId = event.getObservation().getPatient().getId();
        List<DiagnosisEngine.RuleResult> results = diagnosisEngine.evaluateRules(patientId);
        if (!results.isEmpty()) {
            results.forEach(r -> log.info(
                    "Rule inference after {} for patient {}: '{}' via {} (evidence obs: {})",
                    event.getEventType(), patientId,
                    r.inferredConcept, r.strategyUsed, r.evidenceObservationIds));
        }
    }
}
