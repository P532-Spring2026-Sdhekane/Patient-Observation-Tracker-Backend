package com.p532.tracker;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.p532.tracker.domain.*;
import com.p532.tracker.factory.ObservationFactory;
import com.p532.tracker.repository.PhenomenonRepository;
import com.p532.tracker.repository.PhenomenonTypeRepository;
import com.p532.tracker.repository.ProtocolRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ObservationFactoryTest {

    @Mock private PhenomenonTypeRepository phenomenonTypeRepo;
    @Mock private PhenomenonRepository phenomenonRepo;
    @Mock private ProtocolRepository protocolRepo;

    private ObservationFactory factory;
    private final Clock fixedClock = Clock.fixed(Instant.parse("2025-01-15T10:00:00Z"), ZoneOffset.UTC);

    @BeforeEach
    void setUp() {
        factory = new ObservationFactory(phenomenonTypeRepo, phenomenonRepo, protocolRepo, fixedClock);
    }

    // --- Measurement tests ---

    @Test
    void createMeasurement_validInput_returnsMeasurementWithCorrectFields() {
        // Arrange
        PhenomenonType pt = new PhenomenonType("Body Temperature", MeasurementKind.QUANTITATIVE, "°C,°F");
        when(phenomenonTypeRepo.findById(1L)).thenReturn(Optional.of(pt));
        Patient patient = new Patient("Alice", null, null);

        // Act
        Measurement m = factory.createMeasurement(patient, 1L, new BigDecimal("37.5"), "°C", null, null);

        // Assert
        assertNotNull(m);
        assertEquals(new BigDecimal("37.5"), m.getAmount());
        assertEquals("°C", m.getUnit());
        assertEquals(ObservationStatus.ACTIVE, m.getStatus());
        assertEquals(Instant.parse("2025-01-15T10:00:00Z"), m.getRecordingTime());
    }

    @Test
    void createMeasurement_qualitativePhenomenonType_throwsIllegalArgument() {
        // Arrange
        PhenomenonType pt = new PhenomenonType("Blood Group", MeasurementKind.QUALITATIVE, "");
        when(phenomenonTypeRepo.findById(2L)).thenReturn(Optional.of(pt));
        Patient patient = new Patient("Bob", null, null);

        // Act & Assert
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> factory.createMeasurement(patient, 2L, new BigDecimal("1"), "unit", null, null));
        assertTrue(ex.getMessage().contains("not QUANTITATIVE"));
    }

    @Test
    void createMeasurement_unitNotInAllowedSet_throwsIllegalArgument() {
        // Arrange
        PhenomenonType pt = new PhenomenonType("Blood Glucose", MeasurementKind.QUANTITATIVE, "mmol/L,mg/dL");
        when(phenomenonTypeRepo.findById(3L)).thenReturn(Optional.of(pt));
        Patient patient = new Patient("Carol", null, null);

        // Act & Assert
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> factory.createMeasurement(patient, 3L, new BigDecimal("5.5"), "kg", null, null));
        assertTrue(ex.getMessage().contains("not allowed"));
    }

    @Test
    void createMeasurement_phenomenonTypeNotFound_throwsIllegalArgument() {
        // Arrange
        when(phenomenonTypeRepo.findById(99L)).thenReturn(Optional.empty());
        Patient patient = new Patient("Dave", null, null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> factory.createMeasurement(patient, 99L, BigDecimal.ONE, "unit", null, null));
    }

    @Test
    void createMeasurement_applicabilityTimeProvided_usesProvidedTime() {
        // Arrange
        PhenomenonType pt = new PhenomenonType("Systolic BP", MeasurementKind.QUANTITATIVE, "mmHg");
        when(phenomenonTypeRepo.findById(4L)).thenReturn(Optional.of(pt));
        Patient patient = new Patient("Eve", null, null);
        Instant providedTime = Instant.parse("2025-01-10T08:00:00Z");

        // Act
        Measurement m = factory.createMeasurement(patient, 4L, new BigDecimal("120"), "mmHg", providedTime, null);

        // Assert
        assertEquals(providedTime, m.getApplicabilityTime());
    }

    // --- CategoryObservation tests ---

    @Test
    void createCategoryObservation_validInput_returnsCategoryObsWithCorrectFields() {
        // Arrange
        PhenomenonType pt = new PhenomenonType("Blood Group", MeasurementKind.QUALITATIVE, "");
        Phenomenon phenomenon = new Phenomenon("Blood Group A", pt);
        when(phenomenonRepo.findById(1L)).thenReturn(Optional.of(phenomenon));
        Patient patient = new Patient("Frank", null, null);

        // Act
        CategoryObservation co = factory.createCategoryObservation(
                patient, 1L, Presence.PRESENT, null, null);

        // Assert
        assertNotNull(co);
        assertEquals(Presence.PRESENT, co.getPresence());
        assertEquals(ObservationStatus.ACTIVE, co.getStatus());
        assertEquals(phenomenon, co.getPhenomenon());
    }

    @Test
    void createCategoryObservation_phenomenonBelongsToQuantitativeType_throwsIllegalArgument() {
        // Arrange
        PhenomenonType pt = new PhenomenonType("Temperature", MeasurementKind.QUANTITATIVE, "°C");
        Phenomenon phenomenon = new Phenomenon("SomePhenomenon", pt);
        when(phenomenonRepo.findById(5L)).thenReturn(Optional.of(phenomenon));
        Patient patient = new Patient("Grace", null, null);

        // Act & Assert
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> factory.createCategoryObservation(patient, 5L, Presence.PRESENT, null, null));
        assertTrue(ex.getMessage().contains("not QUALITATIVE"));
    }
}
