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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommandTest {

    @Mock private PatientRepository patientRepo;
    @Mock private ObservationRepository observationRepo;
    @Mock private CommandLogEntryRepository commandLogRepo;

    private ObjectMapper objectMapper;
    private CommandLog commandLog;
    private final Clock fixedClock = Clock.fixed(Instant.parse("2025-03-01T12:00:00Z"), ZoneOffset.UTC);

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        commandLog = new CommandLog(commandLogRepo, fixedClock);
    }

    @Test
    void createPatientCommand_execute_savesPatientAndReturnsIt() {
        // Arrange
        Patient expected = new Patient("Alice Smith", null, "test note");
        when(patientRepo.save(any(Patient.class))).thenReturn(expected);
        CreatePatientCommand cmd = new CreatePatientCommand(
                patientRepo, objectMapper, "Alice Smith", null, "test note");

        // Act
        Object result = cmd.execute();

        // Assert
        assertSame(expected, result);
        verify(patientRepo, times(1)).save(any(Patient.class));
    }

    @Test
    void createPatientCommand_getCommandType_returnsCorrectType() {
        // Arrange
        CreatePatientCommand cmd = new CreatePatientCommand(
                patientRepo, objectMapper, "Bob", null, null);

        // Act & Assert
        assertEquals("CREATE_PATIENT", cmd.getCommandType());
    }

    @Test
    void rejectObservationCommand_execute_setsStatusRejectedAndSaves() {
        // Arrange
        Patient patient = new Patient("Carol", null, null);
        Measurement observation = new Measurement();
        observation.setPatient(patient);
        observation.setStatus(ObservationStatus.ACTIVE);
        when(observationRepo.save(observation)).thenReturn(observation);
        RejectObservationCommand cmd = new RejectObservationCommand(
                observationRepo, objectMapper, observation, "Data entry error");

        // Act
        cmd.execute();

        // Assert
        assertEquals(ObservationStatus.REJECTED, observation.getStatus());
        assertEquals("Data entry error", observation.getRejectionReason());
        verify(observationRepo, times(1)).save(observation);
    }

    @Test
    void commandLog_execute_persistsCommandLogEntry() {
        // Arrange
        Patient expected = new Patient("Dave", null, null);
        when(patientRepo.save(any())).thenReturn(expected);

        // CommandLog caches the command by the saved entry's id — must return
        // an entry with a non-null id to avoid NullPointerException in ConcurrentHashMap
        when(commandLogRepo.save(any())).thenAnswer(inv -> {
            CommandLogEntry entry = inv.getArgument(0);
            // Reflectively set id=1 via a subclass stub
            return new CommandLogEntry(
                    entry.getCommandType(), entry.getPayload(),
                    entry.getExecutedAt(), entry.getUser()) {
                @Override public Long getId() { return 1L; }
            };
        });

        CreatePatientCommand cmd = new CreatePatientCommand(
                patientRepo, objectMapper, "Dave", null, null);

        // Act
        commandLog.execute(cmd);

        // Assert
        verify(commandLogRepo, times(1)).save(argThat(entry ->
                "CREATE_PATIENT".equals(entry.getCommandType()) &&
                "staff".equals(entry.getUser())
        ));
    }
}