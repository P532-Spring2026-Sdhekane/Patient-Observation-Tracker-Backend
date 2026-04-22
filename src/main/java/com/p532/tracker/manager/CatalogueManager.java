package com.p532.tracker.manager;

import com.p532.tracker.domain.*;
import com.p532.tracker.repository.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CatalogueManager {

    private final PhenomenonTypeRepository phenomenonTypeRepo;
    private final PhenomenonRepository phenomenonRepo;
    private final ProtocolRepository protocolRepo;
    private final AssociativeFunctionRepository ruleRepo;
    private final CommandLogEntryRepository commandLogRepo;
    private final AuditLogEntryRepository auditLogRepo;

    public CatalogueManager(PhenomenonTypeRepository phenomenonTypeRepo,
                             PhenomenonRepository phenomenonRepo,
                             ProtocolRepository protocolRepo,
                             AssociativeFunctionRepository ruleRepo,
                             CommandLogEntryRepository commandLogRepo,
                             AuditLogEntryRepository auditLogRepo) {
        this.phenomenonTypeRepo = phenomenonTypeRepo;
        this.phenomenonRepo = phenomenonRepo;
        this.protocolRepo = protocolRepo;
        this.ruleRepo = ruleRepo;
        this.commandLogRepo = commandLogRepo;
        this.auditLogRepo = auditLogRepo;
    }

    // --- PhenomenonType ---

    public List<PhenomenonType> getAllPhenomenonTypes() {
        return phenomenonTypeRepo.findAll();
    }

    public PhenomenonType createPhenomenonType(String name, MeasurementKind kind, String allowedUnitsRaw) {
        PhenomenonType pt = new PhenomenonType(name, kind, allowedUnitsRaw);
        return phenomenonTypeRepo.save(pt);
    }

    public PhenomenonType getPhenomenonType(Long id) {
        return phenomenonTypeRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("PhenomenonType not found: " + id));
    }

    // --- Phenomenon ---

    public Phenomenon createPhenomenon(Long phenomenonTypeId, String name) {
        PhenomenonType pt = phenomenonTypeRepo.findById(phenomenonTypeId)
                .orElseThrow(() -> new IllegalArgumentException("PhenomenonType not found: " + phenomenonTypeId));
        if (pt.getKind() != MeasurementKind.QUALITATIVE) {
            throw new IllegalArgumentException("Phenomena can only be added to QUALITATIVE PhenomenonTypes");
        }
        Phenomenon phenomenon = new Phenomenon(name, pt);
        return phenomenonRepo.save(phenomenon);
    }

    public List<Phenomenon> getPhenomenaForType(Long phenomenonTypeId) {
        return phenomenonRepo.findByPhenomenonTypeId(phenomenonTypeId);
    }

    // --- Protocol ---

    public List<Protocol> getAllProtocols() {
        return protocolRepo.findAll();
    }

    public Protocol createProtocol(String name, String description, AccuracyRating accuracyRating) {
        Protocol protocol = new Protocol(name, description, accuracyRating);
        return protocolRepo.save(protocol);
    }

    // --- AssociativeFunction (Diagnostic Rules) ---

    public List<AssociativeFunction> getAllRules() {
        return ruleRepo.findAll();
    }

    public AssociativeFunction createRule(String name, String argumentConceptIds, String productConcept) {
        AssociativeFunction rule = new AssociativeFunction();
        rule.setName(name);
        rule.setArgumentConceptIds(argumentConceptIds);
        rule.setProductConcept(productConcept);
        return ruleRepo.save(rule);
    }

    // --- Logs ---

    public List<CommandLogEntry> getCommandLog() {
        return commandLogRepo.findAllByOrderByExecutedAtDesc();
    }

    public List<AuditLogEntry> getAuditLog() {
        return auditLogRepo.findAllByOrderByTimestampDesc();
    }
}
