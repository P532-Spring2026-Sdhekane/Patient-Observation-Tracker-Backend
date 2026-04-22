package com.p532.tracker.engine;

import com.p532.tracker.domain.*;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * STRATEGY PATTERN — SimpleConjunctiveStrategy (Week 2 updated)
 *
 * Fires when ALL argument PhenomenonType IDs have at least one
 * ACTIVE, MANUAL-source observation for the patient.
 *
 * Week 2 change: filters out INFERRED observations so concept hierarchy
 * propagation (Change 4) does not create circular inference chains.
 */
@Component
public class SimpleConjunctiveStrategy implements DiagnosisStrategy {

    @Override
    public boolean evaluate(AssociativeFunction rule, List<Observation> patientObservations) {
        List<Long> required = rule.getArgumentConceptIdList();
        if (required.isEmpty()) return false;

        Set<Long> coveredTypeIds = new HashSet<>();
        for (Observation obs : patientObservations) {
            if (obs.getStatus() != ObservationStatus.ACTIVE) continue;
            // Week 2: exclude INFERRED observations from evidence
            if (obs.getSource() == ObservationSource.INFERRED) continue;

            if (obs instanceof Measurement m) {
                coveredTypeIds.add(m.getPhenomenonType().getId());
            } else if (obs instanceof CategoryObservation co
                    && co.getPresence() == Presence.PRESENT) {
                coveredTypeIds.add(co.getPhenomenon().getPhenomenonType().getId());
            }
        }
        return coveredTypeIds.containsAll(required);
    }
}
