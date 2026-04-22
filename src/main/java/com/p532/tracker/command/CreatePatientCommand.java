package com.p532.tracker.command;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.p532.tracker.domain.Patient;
import com.p532.tracker.repository.PatientRepository;

import java.time.LocalDate;
import java.util.Map;

public class CreatePatientCommand implements TrackerCommand {

    private final PatientRepository patientRepo;
    private final ObjectMapper      objectMapper;
    private final String            fullName;
    private final LocalDate         dateOfBirth;
    private final String            note;

    public CreatePatientCommand(PatientRepository patientRepo, ObjectMapper objectMapper,
                                 String fullName, LocalDate dateOfBirth, String note) {
        this.patientRepo  = patientRepo;
        this.objectMapper = objectMapper;
        this.fullName     = fullName;
        this.dateOfBirth  = dateOfBirth;
        this.note         = note;
    }

    @Override
    public Object execute() {
        return patientRepo.save(new Patient(fullName, dateOfBirth, note));
    }

    /** Undo is not supported for patient creation (data integrity). */
    @Override
    public Object undo() {
        throw new UnsupportedOperationException("Undo is not supported for CreatePatient");
    }

    @Override public String getCommandType() { return "CREATE_PATIENT"; }

    @Override
    public String getPayloadJson() {
        try {
            return objectMapper.writeValueAsString(Map.of(
                    "fullName",    fullName,
                    "dateOfBirth", dateOfBirth != null ? dateOfBirth.toString() : "",
                    "note",        note != null ? note : ""));
        } catch (Exception e) { return "{}"; }
    }
}
