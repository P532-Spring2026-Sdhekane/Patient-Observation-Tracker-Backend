package com.p532.tracker.decorator;

import com.p532.tracker.domain.PhenomenonType;

import java.math.BigDecimal;

/**
 * DECORATOR PATTERN — AnomalyFlaggingDecorator
 *
 * Compares a measurement's value against the PhenomenonType's
 * normalMin / normalMax range. If the value is outside the range,
 * sets anomalyFlag = true on the request.
 *
 * The observation is still persisted normally — the flag is stored
 * and displayed in the UI to alert clinical staff.
 *
 * Skipped for category observations and when no range is configured.
 */
public class AnomalyFlaggingDecorator extends ObservationProcessorDecorator {

    public AnomalyFlaggingDecorator(ObservationProcessor delegate) {
        super(delegate);
    }

    @Override
    public ObservationRequest process(ObservationRequest request) {
        if ("measurement".equals(request.getObservationKind())) {
            PhenomenonType pt     = request.getPhenomenonType();
            BigDecimal     amount = request.getAmount();

            if (pt != null && amount != null) {
                Double min = pt.getNormalMin();
                Double max = pt.getNormalMax();
                boolean anomaly = false;

                if (min != null && amount.doubleValue() < min) anomaly = true;
                if (max != null && amount.doubleValue() > max) anomaly = true;

                request.setAnomalyFlag(anomaly);
            }
        }
        return delegate.process(request);
    }
}
