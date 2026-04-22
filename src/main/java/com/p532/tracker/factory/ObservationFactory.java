package com.p532.tracker.factory;

import com.p532.tracker.domain.*;
import com.p532.tracker.repository.PhenomenonRepository;
import com.p532.tracker.repository.PhenomenonTypeRepository;
import com.p532.tracker.repository.ProtocolRepository;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;


@Component
public class ObservationFactory {

    private final PhenomenonTypeRepository phenomenonTypeRepo;
    private final PhenomenonRepository phenomenonRepo;
    private final ProtocolRepository protocolRepo;
    private final Clock clock;

    public ObservationFactory(PhenomenonTypeRepository phenomenonTypeRepo,
                               PhenomenonRepository phenomenonRepo,
                               ProtocolRepository protocolRepo,
                               Clock clock) {
        this.phenomenonTypeRepo = phenomenonTypeRepo;
        this.phenomenonRepo = phenomenonRepo;
        this.protocolRepo = protocolRepo;
        this.clock = clock;
    }

    /**
     * Creates a validated Measurement observation.
     *
     * @param patient            the patient this observation belongs to
     * @param phenomenonTypeId   must reference a QUANTITATIVE PhenomenonType
     * @param amount             numeric value
     * @param unit               must be in the PhenomenonType's allowed units
     * @param applicabilityTime  staff-entered time; defaults to now if null
     * @param protocolId         optional
     * @return a fully constructed, unsaved Measurement
     * @throws IllegalArgumentException on any validation failure
     */
    public Measurement createMeasurement(Patient patient,
                                          Long phenomenonTypeId,
                                          BigDecimal amount,
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

        if (unit == null || unit.isBlank()) {
            throw new IllegalArgumentException("Unit must be provided for measurements");
        }

        boolean unitAllowed = pt.getAllowedUnits().stream()
                .map(String::trim)
                .anyMatch(u -> u.equalsIgnoreCase(unit.trim()));

        if (!unitAllowed) {
            throw new IllegalArgumentException(
                    "Unit '" + unit + "' is not allowed for PhenomenonType '" + pt.getName() +
                    "'. Allowed: " + pt.getAllowedUnits());
        }

        Measurement m = new Measurement();
        m.setPatient(patient);
        m.setPhenomenonType(pt);
        m.setAmount(amount);
        m.setUnit(unit.trim());
        m.setRecordingTime(Instant.now(clock));
        m.setApplicabilityTime(applicabilityTime != null ? applicabilityTime : Instant.now(clock));
        m.setStatus(ObservationStatus.ACTIVE);

        if (protocolId != null) {
            Protocol protocol = protocolRepo.findById(protocolId)
                    .orElseThrow(() -> new IllegalArgumentException("Protocol not found: " + protocolId));
            m.setProtocol(protocol);
        }

        return m;
    }

    /**
     * Creates a validated CategoryObservation.
     *
     * @param patient            the patient this observation belongs to
     * @param phenomenonId       must reference an existing Phenomenon
     * @param presence           PRESENT or ABSENT
     * @param applicabilityTime  staff-entered time; defaults to now if null
     * @param protocolId         optional
     * @return a fully constructed, unsaved CategoryObservation
     * @throws IllegalArgumentException on any validation failure
     */
    public CategoryObservation createCategoryObservation(Patient patient,
                                                          Long phenomenonId,
                                                          Presence presence,
                                                          Instant applicabilityTime,
                                                          Long protocolId) {

        Phenomenon phenomenon = phenomenonRepo.findById(phenomenonId)
                .orElseThrow(() -> new IllegalArgumentException("Phenomenon not found: " + phenomenonId));

        PhenomenonType pt = phenomenon.getPhenomenonType();
        if (pt.getKind() != MeasurementKind.QUALITATIVE) {
            throw new IllegalArgumentException(
                    "PhenomenonType '" + pt.getName() + "' is not QUALITATIVE");
        }

        if (presence == null) {
            throw new IllegalArgumentException("Presence must be PRESENT or ABSENT");
        }

        CategoryObservation co = new CategoryObservation();
        co.setPatient(patient);
        co.setPhenomenon(phenomenon);
        co.setPresence(presence);
        co.setRecordingTime(Instant.now(clock));
        co.setApplicabilityTime(applicabilityTime != null ? applicabilityTime : Instant.now(clock));
        co.setStatus(ObservationStatus.ACTIVE);

        if (protocolId != null) {
            Protocol protocol = protocolRepo.findById(protocolId)
                    .orElseThrow(() -> new IllegalArgumentException("Protocol not found: " + protocolId));
            co.setProtocol(protocol);
        }

        return co;
    }
}
