package com.p532.tracker.engine;

import com.p532.tracker.domain.*;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;


@Component
public class SimpleConjunctiveStrategy implements DiagnosisStrategy {

    @Override
    public boolean evaluate(AssociativeFunction rule, List<Observation> patientObservations) {
        List<Long> required = rule.getArgumentConceptIdList();
        if (required.isEmpty()) return false;

        // Build the set of PhenomenonType IDs that are "covered" by active observations
        Set<Long> coveredTypeIds = new java.util.HashSet<>();
        for (Observation obs : patientObservations) {
            if (obs.getStatus() != ObservationStatus.ACTIVE) continue;
            if (obs instanceof Measurement m) {
                coveredTypeIds.add(m.getPhenomenonType().getId());
            } else if (obs instanceof CategoryObservation co
                    && co.getPresence() == Presence.PRESENT) {
                coveredTypeIds.add(co.getPhenomenon().getPhenomenonType().getId());
            }
        }

        // Conjunctive: ALL required concept IDs must be covered
        return coveredTypeIds.containsAll(required);
    }
}
