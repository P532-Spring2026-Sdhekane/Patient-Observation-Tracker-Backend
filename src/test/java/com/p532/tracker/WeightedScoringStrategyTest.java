package com.p532.tracker;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.p532.tracker.domain.*;
import com.p532.tracker.engine.WeightedScoringStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class WeightedScoringStrategyTest {

    private WeightedScoringStrategy strategy;
    private Patient patient;

    @BeforeEach
    void setUp() {
        strategy = new WeightedScoringStrategy(new ObjectMapper());
        patient  = new Patient("Test", null, null);
    }

    private Measurement makeMeasurement(Long typeId) {
        PhenomenonType pt = new PhenomenonType("T-" + typeId, MeasurementKind.QUANTITATIVE, "u") {
            @Override public Long getId() { return typeId; }
        };
        Measurement m = new Measurement();
        m.setPatient(patient);
        m.setPhenomenonType(pt);
        m.setAmount(BigDecimal.ONE);
        m.setUnit("u");
        m.setRecordingTime(Instant.now());
        m.setApplicabilityTime(Instant.now());
        m.setStatus(ObservationStatus.ACTIVE);
        m.setSource(ObservationSource.MANUAL);
        return m;
    }

    private AssociativeFunction makeRule(String weightsJson, double threshold) {
        AssociativeFunction rule = new AssociativeFunction();
        rule.setName("Weighted Rule");
        rule.setArgumentConceptIds("1,2");
        rule.setProductConcept("Risk");
        rule.setStrategyType(StrategyType.WEIGHTED);
        rule.setWeightsJson(weightsJson);
        rule.setThreshold(threshold);
        return rule;
    }

    @Test
    void evaluate_sumOfWeightsExceedsThreshold_returnsTrue() {
        // Arrange — concept 1 weight=0.6, threshold=0.5; concept 1 is present
        AssociativeFunction rule = makeRule("{\"1\":0.6,\"2\":0.4}", 0.5);
        List<Observation> obs = List.of(makeMeasurement(1L));

        // Act
        boolean result = strategy.evaluate(rule, obs);

        // Assert
        assertTrue(result, "Rule should fire when weight 0.6 >= threshold 0.5");
    }

    @Test
    void evaluate_sumOfWeightsBelowThreshold_returnsFalse() {
        // Arrange — only concept 2 present with weight 0.3, threshold 0.5
        AssociativeFunction rule = makeRule("{\"1\":0.7,\"2\":0.3}", 0.5);
        List<Observation> obs = List.of(makeMeasurement(2L));

        // Act
        boolean result = strategy.evaluate(rule, obs);

        // Assert
        assertFalse(result, "Rule should NOT fire when weight 0.3 < threshold 0.5");
    }

    @Test
    void evaluate_inferredObservationNotCounted_returnsFalse() {
        // Arrange — concept 1 present but INFERRED, threshold 0.5
        AssociativeFunction rule = makeRule("{\"1\":0.8}", 0.5);
        Measurement inferred = makeMeasurement(1L);
        inferred.setSource(ObservationSource.INFERRED);
        List<Observation> obs = List.of(inferred);

        // Act
        boolean result = strategy.evaluate(rule, obs);

        // Assert
        assertFalse(result, "INFERRED observations must not count as evidence");
    }
}
