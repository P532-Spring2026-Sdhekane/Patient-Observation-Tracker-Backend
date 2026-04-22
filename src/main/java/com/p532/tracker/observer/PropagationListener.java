package com.p532.tracker.observer;

import com.p532.tracker.domain.*;
import com.p532.tracker.event.ObservationEvent;
import com.p532.tracker.repository.ObservationRepository;
import com.p532.tracker.repository.PhenomenonRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

/**
 * OBSERVER PATTERN — PropagationListener (Week 2, Change 4)
 *
 * Listens for ObservationEvents and enforces concept hierarchy propagation:
 *
 *  PRESENT observation saved:
 *    → For each ancestor of the observed Phenomenon, if no ACTIVE PRESENT
 *      observation already exists for that patient, create an INFERRED PRESENT one.
 *
 *  ABSENT observation saved:
 *    → For each descendant of the observed Phenomenon, if no ACTIVE ABSENT
 *      observation already exists for that patient, create an INFERRED ABSENT one.
 *
 * Inferred observations have source = INFERRED, are stored normally,
 * but are excluded from rule evaluation (handled in strategy implementations).
 *
 * This listener is a zero-touch addition — no changes to AuditLogListener
 * or RuleEvaluationListener were required.
 */
@Component
public class PropagationListener {

    private static final Logger log = LoggerFactory.getLogger(PropagationListener.class);

    private final ObservationRepository observationRepo;
    private final PhenomenonRepository  phenomenonRepo;
    private final Clock                 clock;

    public PropagationListener(ObservationRepository observationRepo,
                                PhenomenonRepository phenomenonRepo,
                                Clock clock) {
        this.observationRepo = observationRepo;
        this.phenomenonRepo  = phenomenonRepo;
        this.clock           = clock;
    }

    @EventListener
    public void onObservationEvent(ObservationEvent event) {
        // Only handle MANUAL CategoryObservations — measurements have no concept hierarchy
        Observation obs = event.getObservation();
        if (!(obs instanceof CategoryObservation co)) return;
        if (obs.getSource() == ObservationSource.INFERRED) return; // prevent infinite loop

        Long patientId = co.getPatient().getId();
        Phenomenon phenomenon = co.getPhenomenon();

        if (co.getPresence() == Presence.PRESENT) {
            propagatePresent(patientId, phenomenon, co);
        } else if (co.getPresence() == Presence.ABSENT) {
            propagateAbsent(patientId, phenomenon, co);
        }
    }

    // ── Propagate PRESENT up to all ancestors ─────────────────────────────────

    private void propagatePresent(Long patientId, Phenomenon phenomenon,
                                   CategoryObservation source) {
        Phenomenon ancestor = phenomenon.getParentConcept();
        while (ancestor != null) {
            if (!hasActivePresentObservation(patientId, ancestor.getId())) {
                saveInferred(patientId, ancestor, Presence.PRESENT, source);
                log.info("Propagated PRESENT to ancestor '{}' for patient {}",
                        ancestor.getName(), patientId);
            }
            ancestor = ancestor.getParentConcept();
        }
    }

    // ── Propagate ABSENT down to all descendants ──────────────────────────────

    private void propagateAbsent(Long patientId, Phenomenon phenomenon,
                                  CategoryObservation source) {
        for (Phenomenon child : phenomenon.getChildConcepts()) {
            if (!hasActiveAbsentObservation(patientId, child.getId())) {
                saveInferred(patientId, child, Presence.ABSENT, source);
                log.info("Propagated ABSENT to descendant '{}' for patient {}",
                        child.getName(), patientId);
            }
            propagateAbsent(patientId, child, source); // recurse
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private boolean hasActivePresentObservation(Long patientId, Long phenomenonId) {
        return observationRepo.findByPatientIdOrderByRecordingTimeDesc(patientId).stream()
                .filter(o -> o instanceof CategoryObservation)
                .map(o -> (CategoryObservation) o)
                .anyMatch(co -> co.getStatus() == ObservationStatus.ACTIVE
                        && co.getPresence() == Presence.PRESENT
                        && co.getPhenomenon().getId().equals(phenomenonId));
    }

    private boolean hasActiveAbsentObservation(Long patientId, Long phenomenonId) {
        return observationRepo.findByPatientIdOrderByRecordingTimeDesc(patientId).stream()
                .filter(o -> o instanceof CategoryObservation)
                .map(o -> (CategoryObservation) o)
                .anyMatch(co -> co.getStatus() == ObservationStatus.ACTIVE
                        && co.getPresence() == Presence.ABSENT
                        && co.getPhenomenon().getId().equals(phenomenonId));
    }

    private void saveInferred(Long patientId, Phenomenon phenomenon,
                               Presence presence, CategoryObservation sourcObs) {
        CategoryObservation inferred = new CategoryObservation();
        inferred.setPatient(sourcObs.getPatient());
        inferred.setPhenomenon(phenomenon);
        inferred.setPresence(presence);
        inferred.setRecordingTime(Instant.now(clock));
        inferred.setApplicabilityTime(Instant.now(clock));
        inferred.setStatus(ObservationStatus.ACTIVE);
        inferred.setSource(ObservationSource.INFERRED);
        inferred.setAnomalyFlag(false);
        observationRepo.save(inferred);
    }
}
