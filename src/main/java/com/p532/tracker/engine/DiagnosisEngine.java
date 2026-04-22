package com.p532.tracker.engine;

import com.p532.tracker.domain.*;
import com.p532.tracker.repository.AssociativeFunctionRepository;
import com.p532.tracker.repository.ObservationRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * ENGINE LAYER — DiagnosisEngine (Week 2)
 *
 * Holds Map<StrategyType, DiagnosisStrategy> — selects strategy per rule.
 * Returns richer RuleResult: inferredConcept + strategyUsed + evidenceIds.
 */
@Service
public class DiagnosisEngine {

    private final Map<StrategyType, DiagnosisStrategy> strategies;
    private final AssociativeFunctionRepository ruleRepo;
    private final ObservationRepository observationRepo;

    public DiagnosisEngine(Map<StrategyType, DiagnosisStrategy> strategies,
                            AssociativeFunctionRepository ruleRepo,
                            ObservationRepository observationRepo) {
        this.strategies      = strategies;
        this.ruleRepo        = ruleRepo;
        this.observationRepo = observationRepo;
    }

    public List<RuleResult> evaluateRules(Long patientId) {
        List<Observation> observations =
                observationRepo.findByPatientIdOrderByRecordingTimeDesc(patientId);
        return evaluate(observations);
    }

    public List<RuleResult> evaluateRulesForObservations(List<Observation> observations) {
        return evaluate(observations);
    }

    private List<RuleResult> evaluate(List<Observation> observations) {
        List<AssociativeFunction> activeRules = ruleRepo.findByActiveTrue();
        List<RuleResult> results = new ArrayList<>();

        for (AssociativeFunction rule : activeRules) {
            StrategyType type = rule.getStrategyType() != null
                    ? rule.getStrategyType() : StrategyType.CONJUNCTIVE;
            DiagnosisStrategy strategy = strategies.getOrDefault(type,
                    strategies.get(StrategyType.CONJUNCTIVE));

            if (strategy != null && strategy.evaluate(rule, observations)) {
                List<Long> evidenceIds = observations.stream()
                        .filter(o -> o.getStatus() == ObservationStatus.ACTIVE)
                        .filter(o -> o.getSource() == ObservationSource.MANUAL)
                        .filter(o -> matchesConcept(o, rule.getArgumentConceptIdList()))
                        .map(Observation::getId)
                        .collect(Collectors.toList());
                results.add(new RuleResult(rule.getProductConcept(), type.name(), evidenceIds));
            }
        }
        return results;
    }

    private boolean matchesConcept(Observation obs, List<Long> conceptIds) {
        if (obs instanceof Measurement m) {
            return conceptIds.contains(m.getPhenomenonType().getId());
        } else if (obs instanceof CategoryObservation co) {
            return conceptIds.contains(co.getPhenomenon().getPhenomenonType().getId());
        }
        return false;
    }

    public static class RuleResult {
        public final String inferredConcept;
        public final String strategyUsed;
        public final List<Long> evidenceObservationIds;

        public RuleResult(String inferredConcept, String strategyUsed, List<Long> evidenceObservationIds) {
            this.inferredConcept        = inferredConcept;
            this.strategyUsed           = strategyUsed;
            this.evidenceObservationIds = evidenceObservationIds;
        }
    }
}
