package com.p532.tracker.event;

import com.p532.tracker.domain.Observation;


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
