package com.p532.tracker;

import com.p532.tracker.decorator.*;
import com.p532.tracker.domain.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class DecoratorChainTest {

    private static final Clock FIXED_CLOCK =
            Clock.fixed(Instant.parse("2025-06-01T09:00:00Z"), ZoneOffset.UTC);

    private ObservationProcessor buildFullChain(String user) {
        return new AuditStampingDecorator(
                new AnomalyFlaggingDecorator(
                        new UnitValidationDecorator(
                                new BaseObservationProcessor())),
                FIXED_CLOCK, user);
    }

    @Test
    void auditStampingDecorator_setsRecordingTimeAndUser() {
        // Arrange
        ObservationProcessor chain = buildFullChain("nurse01");
        ObservationRequest req = new ObservationRequest();
        req.setObservationKind("category");

        // Act
        ObservationRequest result = chain.process(req);

        // Assert
        assertEquals(Instant.parse("2025-06-01T09:00:00Z"), result.getRecordingTime());
        assertEquals("nurse01", result.getActingUser());
    }

    @Test
    void unitValidationDecorator_validUnit_passesThrough() {
        // Arrange
        PhenomenonType pt = new PhenomenonType("Temp", MeasurementKind.QUANTITATIVE, "°C,°F");
        ObservationRequest req = new ObservationRequest();
        req.setObservationKind("measurement");
        req.setUnit("°C");
        req.setPhenomenonType(pt);
        ObservationProcessor chain = new UnitValidationDecorator(new BaseObservationProcessor());

        // Act & Assert — should not throw
        assertDoesNotThrow(() -> chain.process(req));
    }

    @Test
    void unitValidationDecorator_invalidUnit_throwsIllegalArgument() {
        // Arrange
        PhenomenonType pt = new PhenomenonType("Temp", MeasurementKind.QUANTITATIVE, "°C,°F");
        ObservationRequest req = new ObservationRequest();
        req.setObservationKind("measurement");
        req.setUnit("kg");
        req.setPhenomenonType(pt);
        ObservationProcessor chain = new UnitValidationDecorator(new BaseObservationProcessor());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> chain.process(req));
    }

    @Test
    void anomalyFlaggingDecorator_valueOutOfRange_setsAnomalyFlag() {
        // Arrange
        PhenomenonType pt = new PhenomenonType("Temp", MeasurementKind.QUANTITATIVE, "°C");
        pt.setNormalMin(35.0);
        pt.setNormalMax(37.5);

        ObservationRequest req = new ObservationRequest();
        req.setObservationKind("measurement");
        req.setAmount(new BigDecimal("40.0")); // above max
        req.setPhenomenonType(pt);

        ObservationProcessor chain = new AnomalyFlaggingDecorator(new BaseObservationProcessor());

        // Act
        ObservationRequest result = chain.process(req);

        // Assert
        assertTrue(result.isAnomalyFlag(), "Value 40.0 above max 37.5 should be flagged as anomaly");
    }
}
