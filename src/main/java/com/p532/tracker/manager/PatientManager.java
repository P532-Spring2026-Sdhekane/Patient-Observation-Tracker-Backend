package com.p532.tracker.manager;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.p532.tracker.command.CommandLog;
import com.p532.tracker.command.CreatePatientCommand;
import com.p532.tracker.domain.Patient;
import com.p532.tracker.repository.PatientRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * MANAGER LAYER — PatientManager (Week 2 updated)
 *
 * Change 3: createPatient() now accepts actingUser and passes it to CommandLog.
 */
@Service
public class PatientManager {

    private final PatientRepository patientRepo;
    private final CommandLog        commandLog;
    private final ObjectMapper      objectMapper;

    public PatientManager(PatientRepository patientRepo,
                           CommandLog commandLog,
                           ObjectMapper objectMapper) {
        this.patientRepo  = patientRepo;
        this.commandLog   = commandLog;
        this.objectMapper = objectMapper;
    }

    public List<Patient> getAllPatients() {
        return patientRepo.findAll();
    }

    public Optional<Patient> getPatient(Long id) {
        return patientRepo.findById(id);
    }

    public Patient createPatient(String fullName, LocalDate dateOfBirth,
                                  String note, String actingUser) {
        CreatePatientCommand cmd = new CreatePatientCommand(
                patientRepo, objectMapper, fullName, dateOfBirth, note);
        return (Patient) commandLog.execute(cmd,
                actingUser != null ? actingUser : "staff");
    }

    /** Week 1 compat */
    public Patient createPatient(String fullName, LocalDate dateOfBirth, String note) {
        return createPatient(fullName, dateOfBirth, note, "staff");
    }
}
