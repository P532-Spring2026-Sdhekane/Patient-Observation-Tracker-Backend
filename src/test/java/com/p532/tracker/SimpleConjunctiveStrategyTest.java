package com.p532.tracker;

import com.p532.tracker.domain.*;
import com.p532.tracker.engine.SimpleConjunctiveStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class SimpleConjunctiveStrategyTest {

    private SimpleConjunctiveStrategy strategy;
    private Patient patient;

    @BeforeEach
    void setUp() {
        strategy = new SimpleConjunctiveStrategy();
        patient = new Patient("Test Patient", null, null);
    }

    private Measurement makeMeasurement(Long phenomenonTypeId) {
        PhenomenonType pt = new PhenomenonType("Type-" + phenomenonTypeId, MeasurementKind.QUANTITATIVE, "unit");
        // Reflectively set id via a helper subclass approach: use a stub
        PhenomenonType ptWithId = new PhenomenonType("Type-" + phenomenonTypeId, MeasurementKind.QUANTITATIVE, "unit") {
            @Override public Long getId() { return phenomenonTypeId; }
        };
        Measurement m = new Measurement();
        m.setPatient(patient);
        m.setPhenomenonType(ptWithId);
        m.setAmount(BigDecimal.ONE);
        m.setUnit("unit");
        m.setRecordingTime(Instant.now());
        m.setApplicabilityTime(Instant.now());
        m.setStatus(ObservationStatus.ACTIVE);
        return m;
    }

    private AssociativeFunction makeRule(String productConcept, Long... argumentIds) {
        AssociativeFunction rule = new AssociativeFunction();
        rule.setName("Test Rule");
        rule.setProductConcept(productConcept);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < argumentIds.length; i++) {
            if (i > 0) sb.append(",");
            sb.append(argumentIds[i]);
        }
        rule.setArgumentConceptIds(sb.toString());
        return rule;
    }

    @Test
    void evaluate_allArgumentsPresent_returnsTrue() {
        // Arrange
        AssociativeFunction rule = makeRule("Diabetes Risk", 1L, 2L);
        List<Observation> observations = List.of(makeMeasurement(1L), makeMeasurement(2L));

        // Act
        boolean result = strategy.evaluate(rule, observations);

        // Assert
        assertTrue(result, "Rule should fire when all argument concepts are present");
    }

    @Test
    void evaluate_onlyPartialArgumentsPresent_returnsFalse() {
        // Arrange
        AssociativeFunction rule = makeRule("Diabetes Risk", 1L, 2L);
        List<Observation> observations = List.of(makeMeasurement(1L)); // only 1 of 2

        // Act
        boolean result = strategy.evaluate(rule, observations);

        // Assert
        assertFalse(result, "Rule should NOT fire when only some argument concepts are present");
    }

    @Test
    void evaluate_noObservations_returnsFalse() {
        // Arrange
        AssociativeFunction rule = makeRule("SomeInference", 1L);
        List<Observation> observations = List.of();

        // Act
        boolean result = strategy.evaluate(rule, observations);

        // Assert
        assertFalse(result, "Rule should NOT fire with no observations");
    }

    @Test
    void evaluate_rejectedObservationNotCounted_returnsFalse() {
        // Arrange
        AssociativeFunction rule = makeRule("Hypertension Risk", 1L);
        Measurement rejected = makeMeasurement(1L);
        rejected.setStatus(ObservationStatus.REJECTED);
        List<Observation> observations = List.of(rejected);

        // Act
        boolean result = strategy.evaluate(rule, observations);

        // Assert
        assertFalse(result, "Rejected observations must not be counted when evaluating rules");
    }
}
