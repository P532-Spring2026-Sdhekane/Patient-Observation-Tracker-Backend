package com.p532.tracker.engine;

import com.p532.tracker.domain.AssociativeFunction;
import com.p532.tracker.domain.Observation;

import java.util.List;

/**
 * STRATEGY PATTERN — DiagnosisStrategy
 *
 * Defines the algorithm contract for evaluating a single diagnostic rule
 * against a patient's current observations.
 *
 * The signature is intentionally stable across Week 1 and Week 2:
 * WeightedScoringStrategy can be added without changing this interface.
 */
public interface DiagnosisStrategy {

    /**
     * Evaluates whether the given associative function fires for this patient.
     *
     * @param rule                the associative function (rule) to evaluate
     * @param patientObservations the patient's ACTIVE observations
     * @return true if the rule's product concept should be inferred
     */
    boolean evaluate(AssociativeFunction rule, List<Observation> patientObservations);
}
