package com.p532.tracker.domain;

/**
 * Indicates whether an observation was recorded manually by staff
 * or inferred automatically by the PropagationListener (Change 4).
 *
 * DiagnosisStrategy implementations must only count MANUAL observations
 * as evidence to avoid circular inference chains.
 */
public enum ObservationSource {
    MANUAL,
    INFERRED
}
