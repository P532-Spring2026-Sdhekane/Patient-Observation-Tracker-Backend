package com.p532.tracker.controller;

import com.p532.tracker.domain.*;
import com.p532.tracker.manager.CatalogueManager;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class CatalogueController {

    private final CatalogueManager catalogueManager;

    public CatalogueController(CatalogueManager catalogueManager) {
        this.catalogueManager = catalogueManager;
    }

    // --- Phenomenon Types ---

    @GetMapping("/phenomenon-types")
    public List<PhenomenonType> listPhenomenonTypes() {
        return catalogueManager.getAllPhenomenonTypes();
    }

    @PostMapping("/phenomenon-types")
    public ResponseEntity<?> createPhenomenonType(@RequestBody Map<String, String> body) {
        try {
            String name = body.get("name");
            MeasurementKind kind = MeasurementKind.valueOf(body.get("kind").toUpperCase());
            String allowedUnits = body.getOrDefault("allowedUnits", "");
            PhenomenonType pt = catalogueManager.createPhenomenonType(name, kind, allowedUnits);
            return ResponseEntity.ok(pt);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/phenomenon-types/{id}/phenomena")
    public ResponseEntity<?> createPhenomenon(@PathVariable Long id,
                                               @RequestBody Map<String, String> body) {
        try {
            String name = body.get("name");
            Phenomenon ph = catalogueManager.createPhenomenon(id, name);
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

    // --- Protocols ---

    @GetMapping("/protocols")
    public List<Protocol> listProtocols() {
        return catalogueManager.getAllProtocols();
    }

    @PostMapping("/protocols")
    public ResponseEntity<?> createProtocol(@RequestBody Map<String, String> body) {
        try {
            String name = body.get("name");
            String description = body.get("description");
            AccuracyRating rating = AccuracyRating.valueOf(body.get("accuracyRating").toUpperCase());
            Protocol protocol = catalogueManager.createProtocol(name, description, rating);
            return ResponseEntity.ok(protocol);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // --- Diagnostic Rules ---

    @GetMapping("/rules")
    public List<AssociativeFunction> listRules() {
        return catalogueManager.getAllRules();
    }

    @PostMapping("/rules")
    public ResponseEntity<?> createRule(@RequestBody Map<String, String> body) {
        try {
            String name = body.get("name");
            String argumentConceptIds = body.get("argumentConceptIds");
            String productConcept = body.get("productConcept");
            AssociativeFunction rule = catalogueManager.createRule(name, argumentConceptIds, productConcept);
            return ResponseEntity.ok(rule);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
