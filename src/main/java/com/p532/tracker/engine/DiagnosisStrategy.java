package com.p532.tracker.engine;

import com.p532.tracker.domain.AssociativeFunction;
import com.p532.tracker.domain.Observation;

import java.util.List;


public interface DiagnosisStrategy {

    /**
     *
     * @param rule                the associative function (rule) to evaluate
     * @param patientObservations the patient's ACTIVE observations
     * @return true if the rule's product concept should be inferred
     */
    boolean evaluate(AssociativeFunction rule, List<Observation> patientObservations);
}
