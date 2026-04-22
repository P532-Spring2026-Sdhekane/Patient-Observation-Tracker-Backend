package com.p532.tracker.controller;

import com.p532.tracker.domain.Patient;
import com.p532.tracker.manager.PatientManager;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/patients")
public class PatientController {

    private final PatientManager patientManager;

    public PatientController(PatientManager patientManager) {
        this.patientManager = patientManager;
    }

    @GetMapping
    public List<Patient> listPatients() {
        return patientManager.getAllPatients();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Patient> getPatient(@PathVariable Long id) {
        return patientManager.getPatient(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createPatient(@RequestBody Map<String, String> body) {
        try {
            String fullName = body.get("fullName");
            String dobStr = body.get("dateOfBirth");
            String note = body.get("note");

            if (fullName == null || fullName.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("error", "fullName is required"));
            }

            LocalDate dob = (dobStr != null && !dobStr.isBlank()) ? LocalDate.parse(dobStr) : null;
            Patient patient = patientManager.createPatient(fullName, dob, note);
            return ResponseEntity.ok(patient);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
