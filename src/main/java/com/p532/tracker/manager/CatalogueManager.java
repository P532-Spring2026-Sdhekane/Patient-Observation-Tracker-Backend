package com.p532.tracker.manager;

import com.p532.tracker.domain.*;
import com.p532.tracker.repository.*;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * MANAGER LAYER — CatalogueManager (Week 2 updated)
 *
 * Change 1: createRule() now accepts strategyType, weightsJson, threshold
 * Change 4: createPhenomenon() now accepts optional parentConceptId
 */
@Service
public class CatalogueManager {

    private final PhenomenonTypeRepository     phenomenonTypeRepo;
    private final PhenomenonRepository         phenomenonRepo;
    private final ProtocolRepository           protocolRepo;
    private final AssociativeFunctionRepository ruleRepo;
    private final CommandLogEntryRepository    commandLogRepo;
    private final AuditLogEntryRepository      auditLogRepo;

    public CatalogueManager(PhenomenonTypeRepository phenomenonTypeRepo,
                             PhenomenonRepository phenomenonRepo,
                             ProtocolRepository protocolRepo,
                             AssociativeFunctionRepository ruleRepo,
                             CommandLogEntryRepository commandLogRepo,
                             AuditLogEntryRepository auditLogRepo) {
        this.phenomenonTypeRepo = phenomenonTypeRepo;
        this.phenomenonRepo     = phenomenonRepo;
        this.protocolRepo       = protocolRepo;
        this.ruleRepo           = ruleRepo;
        this.commandLogRepo     = commandLogRepo;
        this.auditLogRepo       = auditLogRepo;
    }

    // ── PhenomenonType ────────────────────────────────────────────────────────

    public List<PhenomenonType> getAllPhenomenonTypes() {
        return phenomenonTypeRepo.findAll();
    }

    public PhenomenonType createPhenomenonType(String name, MeasurementKind kind,
                                                String allowedUnitsRaw,
                                                Double normalMin, Double normalMax) {
        PhenomenonType pt = new PhenomenonType(name, kind, allowedUnitsRaw);
        pt.setNormalMin(normalMin);
        pt.setNormalMax(normalMax);
        return phenomenonTypeRepo.save(pt);
    }

    /** Week 1 compat — no normal range */
    public PhenomenonType createPhenomenonType(String name, MeasurementKind kind,
                                                String allowedUnitsRaw) {
        return createPhenomenonType(name, kind, allowedUnitsRaw, null, null);
    }

    public PhenomenonType getPhenomenonType(Long id) {
        return phenomenonTypeRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("PhenomenonType not found: " + id));
    }

    // ── Phenomenon ────────────────────────────────────────────────────────────

    /**
     * Change 4: accepts optional parentConceptId for concept hierarchy.
     */
    public Phenomenon createPhenomenon(Long phenomenonTypeId, String name,
                                        Long parentConceptId) {
        PhenomenonType pt = phenomenonTypeRepo.findById(phenomenonTypeId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "PhenomenonType not found: " + phenomenonTypeId));
        if (pt.getKind() != MeasurementKind.QUALITATIVE) {
            throw new IllegalArgumentException(
                    "Phenomena can only be added to QUALITATIVE PhenomenonTypes");
        }
        Phenomenon phenomenon = new Phenomenon(name, pt);
        if (parentConceptId != null) {
            Phenomenon parent = phenomenonRepo.findById(parentConceptId)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Parent phenomenon not found: " + parentConceptId));
            phenomenon.setParentConcept(parent);
        }
        return phenomenonRepo.save(phenomenon);
    }

    /** Week 1 compat — no parent */
    public Phenomenon createPhenomenon(Long phenomenonTypeId, String name) {
        return createPhenomenon(phenomenonTypeId, name, null);
    }

    public List<Phenomenon> getPhenomenaForType(Long phenomenonTypeId) {
        return phenomenonRepo.findByPhenomenonTypeId(phenomenonTypeId);
    }

    // ── Protocol ──────────────────────────────────────────────────────────────

    public List<Protocol> getAllProtocols() {
        return protocolRepo.findAll();
    }

    public Protocol createProtocol(String name, String description,
                                    AccuracyRating accuracyRating) {
        return protocolRepo.save(new Protocol(name, description, accuracyRating));
    }

    // ── AssociativeFunction (Diagnostic Rules) ────────────────────────────────

    public List<AssociativeFunction> getAllRules() {
        return ruleRepo.findAll();
    }

    /**
     * Change 1: accepts strategyType, weightsJson, threshold.
     */
    public AssociativeFunction createRule(String name,
                                           String argumentConceptIds,
                                           String productConcept,
                                           StrategyType strategyType,
                                           String weightsJson,
                                           Double threshold) {
        AssociativeFunction rule = new AssociativeFunction();
        rule.setName(name);
        rule.setArgumentConceptIds(argumentConceptIds);
        rule.setProductConcept(productConcept);
        rule.setStrategyType(strategyType != null ? strategyType : StrategyType.CONJUNCTIVE);
        rule.setWeightsJson(weightsJson);
        rule.setThreshold(threshold != null ? threshold : 0.5);
        return ruleRepo.save(rule);
    }

    /** Week 1 compat — defaults to CONJUNCTIVE strategy */
    public AssociativeFunction createRule(String name, String argumentConceptIds,
                                           String productConcept) {
        return createRule(name, argumentConceptIds, productConcept,
                StrategyType.CONJUNCTIVE, null, null);
    }

    // ── Logs ──────────────────────────────────────────────────────────────────

    public List<CommandLogEntry> getCommandLog() {
        return commandLogRepo.findAllByOrderByExecutedAtDesc();
    }

    public List<AuditLogEntry> getAuditLog() {
        return auditLogRepo.findAllByOrderByTimestampDesc();
    }
}
