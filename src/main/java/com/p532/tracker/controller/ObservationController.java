package com.p532.tracker.controller;

import com.p532.tracker.domain.*;
import com.p532.tracker.manager.ObservationManager;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;


@RestController
public class ObservationController {

    private final ObservationManager observationManager;

    public ObservationController(ObservationManager observationManager) {
        this.observationManager = observationManager;
    }

    @GetMapping("/api/patients/{id}/observations")
    public List<Observation> listObservations(@PathVariable Long id) {
        return observationManager.getObservationsForPatient(id);
    }

    @PostMapping("/api/observations/measurement")
    public ResponseEntity<?> recordMeasurement(@RequestBody Map<String, Object> body) {
        try {
            Long patientId = toLong(body.get("patientId"));
            Long phenomenonTypeId = toLong(body.get("phenomenonTypeId"));
            BigDecimal amount = new BigDecimal(body.get("amount").toString());
            String unit = (String) body.get("unit");
            String applicabilityStr = (String) body.get("applicabilityTime");
            Instant applicabilityTime = (applicabilityStr != null && !applicabilityStr.isBlank())
                    ? Instant.parse(applicabilityStr) : null;
            Long protocolId = body.get("protocolId") != null ? toLong(body.get("protocolId")) : null;

            Measurement saved = observationManager.recordMeasurement(
                    patientId, phenomenonTypeId, amount, unit, applicabilityTime, protocolId);
            return ResponseEntity.ok(saved);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/api/observations/category")
    public ResponseEntity<?> recordCategoryObservation(@RequestBody Map<String, Object> body) {
        try {
            Long patientId = toLong(body.get("patientId"));
            Long phenomenonId = toLong(body.get("phenomenonId"));
            Presence presence = Presence.valueOf(((String) body.get("presence")).toUpperCase());
            String applicabilityStr = (String) body.get("applicabilityTime");
            Instant applicabilityTime = (applicabilityStr != null && !applicabilityStr.isBlank())
                    ? Instant.parse(applicabilityStr) : null;
            Long protocolId = body.get("protocolId") != null ? toLong(body.get("protocolId")) : null;

            CategoryObservation saved = observationManager.recordCategoryObservation(
                    patientId, phenomenonId, presence, applicabilityTime, protocolId);
            return ResponseEntity.ok(saved);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/api/observations/{id}/reject")
    public ResponseEntity<?> rejectObservation(@PathVariable Long id,
                                                @RequestBody Map<String, String> body) {
        try {
            String reason = body.getOrDefault("reason", "");
            Observation rejected = observationManager.rejectObservation(id, reason);
            return ResponseEntity.ok(rejected);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/api/patients/{id}/evaluate")
    public ResponseEntity<?> evaluateRules(@PathVariable Long id) {
        try {
            List<String> inferences = observationManager.evaluateRules(id);
            return ResponseEntity.ok(Map.of("inferredConcepts", inferences));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    private Long toLong(Object value) {
        if (value == null) throw new IllegalArgumentException("Required field is null");
        return Long.parseLong(value.toString());
    }
}
