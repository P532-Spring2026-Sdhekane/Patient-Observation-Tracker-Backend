package com.p532.tracker.factory;

import com.p532.tracker.decorator.ObservationRequest;
import com.p532.tracker.domain.*;
import com.p532.tracker.repository.PhenomenonRepository;
import com.p532.tracker.repository.PhenomenonTypeRepository;
import com.p532.tracker.repository.ProtocolRepository;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;

/**
 * FACTORY PATTERN — ObservationFactory (Week 2 updated)
 *
 * Constructs Measurement and CategoryObservation from a processed
 * ObservationRequest. The decorator chain has already:
 *  - stamped recordingTime and actingUser (AuditStampingDecorator)
 *  - validated the unit (UnitValidationDecorator)
 *  - set the anomalyFlag (AnomalyFlaggingDecorator)
 *
 * Week 2 changes:
 *  - Unit validation removed (now in UnitValidationDecorator)
 *  - Accepts ObservationRequest directly (in addition to raw params)
 *  - Applies source and anomalyFlag from the request to the entity
 */
@Component
public class ObservationFactory {

    private final PhenomenonTypeRepository phenomenonTypeRepo;
    private final PhenomenonRepository     phenomenonRepo;
    private final ProtocolRepository       protocolRepo;
    private final Clock                    clock;

    public ObservationFactory(PhenomenonTypeRepository phenomenonTypeRepo,
                               PhenomenonRepository phenomenonRepo,
                               ProtocolRepository protocolRepo,
                               Clock clock) {
        this.phenomenonTypeRepo = phenomenonTypeRepo;
        this.phenomenonRepo     = phenomenonRepo;
        this.protocolRepo       = protocolRepo;
        this.clock              = clock;
    }

    // ── Week 2 primary entry point: build from processed request ─────────────

    public Measurement createMeasurementFromRequest(Patient patient, ObservationRequest req) {
        PhenomenonType pt = phenomenonTypeRepo.findById(req.getPhenomenonTypeId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "PhenomenonType not found: " + req.getPhenomenonTypeId()));

        if (pt.getKind() != MeasurementKind.QUANTITATIVE) {
            throw new IllegalArgumentException(
                    "PhenomenonType '" + pt.getName() + "' is not QUANTITATIVE");
        }

        Measurement m = new Measurement();
        m.setPatient(patient);
        m.setPhenomenonType(pt);
        m.setAmount(req.getAmount());
        m.setUnit(req.getUnit() != null ? req.getUnit().trim() : null);
        m.setRecordingTime(req.getRecordingTime() != null
                ? req.getRecordingTime() : Instant.now(clock));
        m.setApplicabilityTime(req.getApplicabilityTime() != null
                ? req.getApplicabilityTime() : Instant.now(clock));
        m.setStatus(ObservationStatus.ACTIVE);
        m.setSource(req.getSource() != null ? req.getSource() : ObservationSource.MANUAL);
        m.setAnomalyFlag(req.isAnomalyFlag());

        if (req.getProtocolId() != null) {
            m.setProtocol(protocolRepo.findById(req.getProtocolId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Protocol not found: " + req.getProtocolId())));
        }
        return m;
    }

    public CategoryObservation createCategoryObservationFromRequest(Patient patient, ObservationRequest req) {
        Phenomenon phenomenon = phenomenonRepo.findById(req.getPhenomenonId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Phenomenon not found: " + req.getPhenomenonId()));

        PhenomenonType pt = phenomenon.getPhenomenonType();
        if (pt.getKind() != MeasurementKind.QUALITATIVE) {
            throw new IllegalArgumentException(
                    "PhenomenonType '" + pt.getName() + "' is not QUALITATIVE");
        }

        Presence presence = Presence.valueOf(req.getPresence().toUpperCase());

        CategoryObservation co = new CategoryObservation();
        co.setPatient(patient);
        co.setPhenomenon(phenomenon);
        co.setPresence(presence);
        co.setRecordingTime(req.getRecordingTime() != null
                ? req.getRecordingTime() : Instant.now(clock));
        co.setApplicabilityTime(req.getApplicabilityTime() != null
                ? req.getApplicabilityTime() : Instant.now(clock));
        co.setStatus(ObservationStatus.ACTIVE);
        co.setSource(req.getSource() != null ? req.getSource() : ObservationSource.MANUAL);
        co.setAnomalyFlag(false);

        if (req.getProtocolId() != null) {
            co.setProtocol(protocolRepo.findById(req.getProtocolId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Protocol not found: " + req.getProtocolId())));
        }
        return co;
    }

    // ── Week 1 compatibility methods (still used by tests) ───────────────────

    public Measurement createMeasurement(Patient patient,
                                          Long phenomenonTypeId,
                                          java.math.BigDecimal amount,
                                          String unit,
                                          Instant applicabilityTime,
                                          Long protocolId) {
        PhenomenonType pt = phenomenonTypeRepo.findById(phenomenonTypeId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "PhenomenonType not found: " + phenomenonTypeId));

        if (pt.getKind() != MeasurementKind.QUANTITATIVE) {
            throw new IllegalArgumentException(
                    "PhenomenonType '" + pt.getName() + "' is not QUANTITATIVE");
        }

        boolean unitAllowed = pt.getAllowedUnits().stream()
                .map(String::trim)
                .anyMatch(u -> u.equalsIgnoreCase(unit != null ? unit.trim() : ""));
        if (!unitAllowed) {
            throw new IllegalArgumentException(
                    "Unit '" + unit + "' is not allowed for '" + pt.getName() + "'");
        }

        Measurement m = new Measurement();
        m.setPatient(patient);
        m.setPhenomenonType(pt);
        m.setAmount(amount);
        m.setUnit(unit != null ? unit.trim() : null);
        m.setRecordingTime(Instant.now(clock));
        m.setApplicabilityTime(applicabilityTime != null ? applicabilityTime : Instant.now(clock));
        m.setStatus(ObservationStatus.ACTIVE);
        m.setSource(ObservationSource.MANUAL);

        if (protocolId != null) {
            m.setProtocol(protocolRepo.findById(protocolId)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Protocol not found: " + protocolId)));
        }
        return m;
    }

    public CategoryObservation createCategoryObservation(Patient patient,
                                                          Long phenomenonId,
                                                          Presence presence,
                                                          Instant applicabilityTime,
                                                          Long protocolId) {
        Phenomenon phenomenon = phenomenonRepo.findById(phenomenonId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Phenomenon not found: " + phenomenonId));

        PhenomenonType pt = phenomenon.getPhenomenonType();
        if (pt.getKind() != MeasurementKind.QUALITATIVE) {
            throw new IllegalArgumentException(
                    "PhenomenonType '" + pt.getName() + "' is not QUALITATIVE");
        }

        CategoryObservation co = new CategoryObservation();
        co.setPatient(patient);
        co.setPhenomenon(phenomenon);
        co.setPresence(presence);
        co.setRecordingTime(Instant.now(clock));
        co.setApplicabilityTime(applicabilityTime != null ? applicabilityTime : Instant.now(clock));
        co.setStatus(ObservationStatus.ACTIVE);
        co.setSource(ObservationSource.MANUAL);

        if (protocolId != null) {
            co.setProtocol(protocolRepo.findById(protocolId)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Protocol not found: " + protocolId)));
        }
        return co;
    }
}
