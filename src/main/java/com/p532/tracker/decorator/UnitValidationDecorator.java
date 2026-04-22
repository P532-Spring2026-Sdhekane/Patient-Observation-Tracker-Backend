package com.p532.tracker.decorator;

import com.p532.tracker.domain.MeasurementKind;
import com.p532.tracker.domain.PhenomenonType;

/**
 * DECORATOR PATTERN — UnitValidationDecorator
 *
 * Validates that a measurement's unit is in the PhenomenonType's
 * allowed-units set. Throws IllegalArgumentException if not.
 *
 * Week 2: this logic was moved here from ObservationFactory so the
 * factory focuses purely on object construction.
 *
 * Skipped for category observations (unit is irrelevant).
 */
public class UnitValidationDecorator extends ObservationProcessorDecorator {

    public UnitValidationDecorator(ObservationProcessor delegate) {
        super(delegate);
    }

    @Override
    public ObservationRequest process(ObservationRequest request) {
        if ("measurement".equals(request.getObservationKind())) {
            PhenomenonType pt = request.getPhenomenonType();
            String unit = request.getUnit();

            if (pt != null && pt.getKind() == MeasurementKind.QUANTITATIVE) {
                if (unit == null || unit.isBlank()) {
                    throw new IllegalArgumentException("Unit is required for measurements");
                }
                boolean allowed = pt.getAllowedUnits().stream()
                        .map(String::trim)
                        .anyMatch(u -> u.equalsIgnoreCase(unit.trim()));
                if (!allowed) {
                    throw new IllegalArgumentException(
                            "Unit '" + unit + "' is not in the allowed set for '"
                            + pt.getName() + "': " + pt.getAllowedUnits());
                }
            }
        }
        return delegate.process(request);
    }
}
