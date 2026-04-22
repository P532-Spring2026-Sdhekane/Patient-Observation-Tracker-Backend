package com.p532.tracker.engine;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.p532.tracker.domain.*;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * STRATEGY PATTERN — WeightedScoringStrategy (Week 2 concrete strategy)
 *
 * Fires a rule when the sum of weights of currently ACTIVE,
 * MANUAL-source argument concepts equals or exceeds the rule's threshold.
 *
 * Example: rule has weights {"1": 0.6, "2": 0.4} and threshold 0.5.
 * If concept 1 is present → score = 0.6 >= 0.5 → rule fires.
 * If only concept 2 is present → score = 0.4 < 0.5 → rule does not fire.
 */
@Component
public class WeightedScoringStrategy implements DiagnosisStrategy {

    private final ObjectMapper objectMapper;

    public WeightedScoringStrategy(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean evaluate(AssociativeFunction rule, List<Observation> patientObservations) {
        String weightsJson = rule.getWeightsJson();
        Double threshold   = rule.getThreshold();

        if (weightsJson == null || weightsJson.isBlank()) return false;
        if (threshold   == null) threshold = 0.5;

        // Parse weight map: phenomenonTypeId (string) -> weight
        Map<String, Double> weights;
        try {
            weights = objectMapper.readValue(weightsJson,
                    new TypeReference<Map<String, Double>>() {});
        } catch (Exception e) {
            return false;
        }

        // Collect covered PhenomenonType IDs from ACTIVE, MANUAL observations only
        Set<Long> coveredTypeIds = getCoveredTypeIds(patientObservations);

        // Sum weights of covered concepts
        double score = 0.0;
        for (Map.Entry<String, Double> entry : weights.entrySet()) {
            try {
                Long typeId = Long.parseLong(entry.getKey());
                if (coveredTypeIds.contains(typeId)) {
                    score += entry.getValue();
                }
            } catch (NumberFormatException ignored) {}
        }

        return score >= threshold;
    }

    private Set<Long> getCoveredTypeIds(List<Observation> observations) {
        return observations.stream()
                .filter(o -> o.getStatus() == ObservationStatus.ACTIVE)
                .filter(o -> o.getSource() == ObservationSource.MANUAL)
                .collect(java.util.stream.Collectors.toSet())
                .stream()
                .mapMulti((Observation obs, java.util.function.Consumer<Long> consumer) -> {
                    if (obs instanceof Measurement m) {
                        consumer.accept(m.getPhenomenonType().getId());
                    } else if (obs instanceof CategoryObservation co
                            && co.getPresence() == Presence.PRESENT) {
                        consumer.accept(co.getPhenomenon().getPhenomenonType().getId());
                    }
                })
                .collect(Collectors.toSet());
    }
}
