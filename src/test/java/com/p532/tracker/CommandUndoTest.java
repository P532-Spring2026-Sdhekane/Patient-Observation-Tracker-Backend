package com.p532.tracker;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.p532.tracker.command.*;
import com.p532.tracker.domain.*;
import com.p532.tracker.repository.CommandLogEntryRepository;
import com.p532.tracker.repository.ObservationRepository;
import com.p532.tracker.repository.PatientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommandUndoTest {

    @Mock private ObservationRepository    observationRepo;
    @Mock private CommandLogEntryRepository commandLogRepo;
    @Mock private PatientRepository        patientRepo;

    private ObjectMapper objectMapper;
    private CommandLog   commandLog;

    private static final Clock FIXED_CLOCK =
            Clock.fixed(Instant.parse("2025-09-01T12:00:00Z"), ZoneOffset.UTC);

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        commandLog   = new CommandLog(commandLogRepo, FIXED_CLOCK);
    }

    private Measurement buildMeasurement() {
        Patient patient = new Patient("Alice", null, null);
        Measurement m   = new Measurement();
        m.setPatient(patient);
        m.setStatus(ObservationStatus.ACTIVE);
        m.setSource(ObservationSource.MANUAL);
        return m;
    }

    @Test
    void recordObservationCommand_undo_marksObservationRejected() {
        // Arrange
        Measurement m = buildMeasurement();
        when(observationRepo.save(any())).thenReturn(m);
        RecordObservationCommand cmd = new RecordObservationCommand(
                observationRepo, objectMapper, m);
        cmd.execute(); // must execute first so saved reference is set

        // Act
        cmd.undo();

        // Assert
        assertEquals(ObservationStatus.REJECTED, m.getStatus());
        assertEquals("Undone by user", m.getRejectionReason());
    }

    @Test
    void rejectObservationCommand_undo_restoresObservationToActive() {
        // Arrange
        Measurement m = buildMeasurement();
        m.setStatus(ObservationStatus.REJECTED);
        m.setRejectionReason("Error");
        when(observationRepo.save(any())).thenReturn(m);
        RejectObservationCommand cmd = new RejectObservationCommand(
                observationRepo, objectMapper, m, "Error");

        // Act
        cmd.undo();

        // Assert
        assertEquals(ObservationStatus.ACTIVE, m.getStatus());
        assertNull(m.getRejectionReason());
    }

    @Test
    void createPatientCommand_undo_throwsUnsupportedOperation() {
        // Arrange
        when(patientRepo.save(any())).thenReturn(new Patient("Bob", null, null));
        CreatePatientCommand cmd = new CreatePatientCommand(
                patientRepo, objectMapper, "Bob", null, null);
        cmd.execute();

        // Act & Assert
        assertThrows(UnsupportedOperationException.class, cmd::undo,
                "Undo of CreatePatient should not be supported");
    }
}
