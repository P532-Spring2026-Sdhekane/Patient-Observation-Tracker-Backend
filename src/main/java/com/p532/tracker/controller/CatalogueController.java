package com.p532.tracker.controller;

import com.p532.tracker.domain.*;
import com.p532.tracker.manager.CatalogueManager;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * CLIENT LAYER — CatalogueController (Week 2 updated)
 *
 * Change 1: POST /api/rules accepts strategyType, weightsJson, threshold
 * Change 2: POST /api/phenomenon-types accepts normalMin, normalMax
 * Change 4: POST /api/phenomenon-types/{id}/phenomena accepts parentConceptId
 */
@RestController
@RequestMapping("/api")
public class CatalogueController {

    private final CatalogueManager catalogueManager;

    public CatalogueController(CatalogueManager catalogueManager) {
        this.catalogueManager = catalogueManager;
    }

    // ── Phenomenon Types ──────────────────────────────────────────────────────

    @GetMapping("/phenomenon-types")
    public List<PhenomenonType> listPhenomenonTypes() {
        return catalogueManager.getAllPhenomenonTypes();
    }

    @PostMapping("/phenomenon-types")
    public ResponseEntity<?> createPhenomenonType(@RequestBody Map<String, Object> body) {
        try {
            String name         = (String) body.get("name");
            MeasurementKind kind = MeasurementKind.valueOf(
                    ((String) body.get("kind")).toUpperCase());
            String allowedUnits = (String) body.getOrDefault("allowedUnits", "");

            // Week 2: optional normal range
            Double normalMin = body.get("normalMin") != null
                    ? Double.parseDouble(body.get("normalMin").toString()) : null;
            Double normalMax = body.get("normalMax") != null
                    ? Double.parseDouble(body.get("normalMax").toString()) : null;

            PhenomenonType pt = catalogueManager.createPhenomenonType(
                    name, kind, allowedUnits, normalMin, normalMax);
            return ResponseEntity.ok(pt);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ── Phenomena ─────────────────────────────────────────────────────────────

    @PostMapping("/phenomenon-types/{id}/phenomena")
    public ResponseEntity<?> createPhenomenon(@PathVariable Long id,
                                               @RequestBody Map<String, Object> body) {
        try {
            String name = (String) body.get("name");
            // Week 2: optional parentConceptId
            Long parentConceptId = body.get("parentConceptId") != null
                    ? Long.parseLong(body.get("parentConceptId").toString()) : null;
            Phenomenon ph = catalogueManager.createPhenomenon(id, name, parentConceptId);
            return ResponseEntity.ok(ph);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/phenomenon-types/{id}/phenomena")
    public ResponseEntity<?> listPhenomena(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(catalogueManager.getPhenomenaForType(id));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ── Protocols ─────────────────────────────────────────────────────────────

    @GetMapping("/protocols")
    public List<Protocol> listProtocols() {
        return catalogueManager.getAllProtocols();
    }

    @PostMapping("/protocols")
    public ResponseEntity<?> createProtocol(@RequestBody Map<String, String> body) {
        try {
            String name          = body.get("name");
            String description   = body.get("description");
            AccuracyRating rating = AccuracyRating.valueOf(
                    body.get("accuracyRating").toUpperCase());
            return ResponseEntity.ok(
                    catalogueManager.createProtocol(name, description, rating));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ── Diagnostic Rules ──────────────────────────────────────────────────────

    @GetMapping("/rules")
    public List<AssociativeFunction> listRules() {
        return catalogueManager.getAllRules();
    }

    @PostMapping("/rules")
    public ResponseEntity<?> createRule(@RequestBody Map<String, Object> body) {
        try {
            String name               = (String) body.get("name");
            String argumentConceptIds = (String) body.get("argumentConceptIds");
            String productConcept     = (String) body.get("productConcept");

            // Week 2: strategy fields (all optional — default CONJUNCTIVE)
            StrategyType strategyType = body.get("strategyType") != null
                    ? StrategyType.valueOf(body.get("strategyType").toString().toUpperCase())
                    : StrategyType.CONJUNCTIVE;
            String weightsJson = (String) body.get("weightsJson");
            Double threshold   = body.get("threshold") != null
                    ? Double.parseDouble(body.get("threshold").toString()) : null;

            AssociativeFunction rule = catalogueManager.createRule(
                    name, argumentConceptIds, productConcept,
                    strategyType, weightsJson, threshold);
            return ResponseEntity.ok(rule);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
