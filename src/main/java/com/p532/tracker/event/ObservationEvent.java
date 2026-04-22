package com.p532.tracker.event;

import com.p532.tracker.domain.Observation;

/**
 * OBSERVER PATTERN — ObservationEvent
 *
 * Published by ObservationManager via Spring's ApplicationEventPublisher
 * whenever an observation is saved (created) or rejected.
 *
 * Spring's @EventListener decouples listeners from the publisher.
 * Adding new listeners in Week 2 (PropagationListener) requires zero
 * changes to existing code.
 */
public class ObservationEvent {

    public enum Type { CREATED, REJECTED }

    private final Observation observation;
    private final Type eventType;

    public ObservationEvent(Observation observation, Type eventType) {
        this.observation = observation;
        this.eventType = eventType;
    }

    public Observation getObservation() { return observation; }
    public Type getEventType() { return eventType; }
}
