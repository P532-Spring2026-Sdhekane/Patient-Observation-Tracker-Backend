package com.p532.tracker.engine;

import com.p532.tracker.domain.AssociativeFunction;
import com.p532.tracker.domain.Observation;
import com.p532.tracker.repository.AssociativeFunctionRepository;
import com.p532.tracker.repository.ObservationRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;


@Service
public class DiagnosisEngine {

    private final DiagnosisStrategy strategy;
    private final AssociativeFunctionRepository ruleRepo;
    private final ObservationRepository observationRepo;

    public DiagnosisEngine(DiagnosisStrategy strategy,
                            AssociativeFunctionRepository ruleRepo,
                            ObservationRepository observationRepo) {
        this.strategy = strategy;
        this.ruleRepo = ruleRepo;
        this.observationRepo = observationRepo;
    }

    /**
     * Runs all active rules against the patient's observations.
     *
     * @param patientId the patient to evaluate
     * @return list of inferred product-concept names (not saved as observations)
     */
    public List<String> evaluateRules(Long patientId) {
        List<Observation> observations = observationRepo
                .findByPatientIdOrderByRecordingTimeDesc(patientId);

        List<AssociativeFunction> activeRules = ruleRepo.findByActiveTrue();

        return activeRules.stream()
                .filter(rule -> strategy.evaluate(rule, observations))
                .map(AssociativeFunction::getProductConcept)
                .collect(Collectors.toList());
    }

    public List<String> evaluateRulesForObservations(Long patientId,
                                                       List<Observation> observations) {
        List<AssociativeFunction> activeRules = ruleRepo.findByActiveTrue();

        return activeRules.stream()
                .filter(rule -> strategy.evaluate(rule, observations))
                .map(AssociativeFunction::getProductConcept)
                .collect(Collectors.toList());
    }
}
