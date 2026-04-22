package com.p532.tracker.decorator;

/**
 * DECORATOR PATTERN — ObservationProcessor
 *
 * Single-method interface implemented by BaseObservationProcessor
 * and all decorator classes. Each decorator wraps a delegate and
 * adds one concern (validation, anomaly flagging, audit stamping).
 */
public interface ObservationProcessor {
    ObservationRequest process(ObservationRequest request);
}
