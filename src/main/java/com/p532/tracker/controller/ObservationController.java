package com.p532.tracker.controller;

import com.p532.tracker.domain.*;
import com.p532.tracker.engine.DiagnosisEngine;
import com.p532.tracker.manager.ObservationManager;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * CLIENT LAYER — ObservationController (Week 2 updated)
 *
 * Change 1: /evaluate now returns List<RuleResult> with strategyUsed + evidenceIds
 * Change 3: reads X-Acting-User header to pass real username to manager
 */
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
    public ResponseEntity<?> recordMeasurement(
            @RequestBody Map<String, Object> body,
            @RequestHeader(value = "X-Acting-User", defaultValue = "staff") String actingUser) {
        try {
            Long patientId        = toLong(body.get("patientId"));
            Long phenomenonTypeId = toLong(body.get("phenomenonTypeId"));
            BigDecimal amount     = new BigDecimal(body.get("amount").toString());
            String unit           = (String) body.get("unit");
            String appStr         = (String) body.get("applicabilityTime");
            Instant applicability = appStr != null && !appStr.isBlank()
                    ? Instant.parse(appStr) : null;
            Long protocolId       = body.get("protocolId") != null
                    ? toLong(body.get("protocolId")) : null;

            Measurement saved = observationManager.recordMeasurement(
                    patientId, phenomenonTypeId, amount, unit,
                    applicability, protocolId, actingUser);
            return ResponseEntity.ok(saved);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/api/observations/category")
    public ResponseEntity<?> recordCategoryObservation(
            @RequestBody Map<String, Object> body,
            @RequestHeader(value = "X-Acting-User", defaultValue = "staff") String actingUser) {
        try {
            Long patientId    = toLong(body.get("patientId"));
            Long phenomenonId = toLong(body.get("phenomenonId"));
            Presence presence = Presence.valueOf(
                    ((String) body.get("presence")).toUpperCase());
            String appStr     = (String) body.get("applicabilityTime");
            Instant applicability = appStr != null && !appStr.isBlank()
                    ? Instant.parse(appStr) : null;
            Long protocolId   = body.get("protocolId") != null
                    ? toLong(body.get("protocolId")) : null;

            CategoryObservation saved = observationManager.recordCategoryObservation(
                    patientId, phenomenonId, presence, applicability, protocolId, actingUser);
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
            String reason       = body.getOrDefault("reason", "");
            Observation rejected = observationManager.rejectObservation(id, reason);
            return ResponseEntity.ok(rejected);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Change 1: returns richer structure per fired rule:
     *   { inferredConcept, strategyUsed, evidenceObservationIds }
     */
    @PostMapping("/api/patients/{id}/evaluate")
    public ResponseEntity<?> evaluateRules(@PathVariable Long id) {
        try {
            List<DiagnosisEngine.RuleResult> results = observationManager.evaluateRules(id);
            return ResponseEntity.ok(Map.of("inferredConcepts", results));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    private Long toLong(Object value) {
        if (value == null) throw new IllegalArgumentException("Required field is null");
        return Long.parseLong(value.toString());
    }
}
