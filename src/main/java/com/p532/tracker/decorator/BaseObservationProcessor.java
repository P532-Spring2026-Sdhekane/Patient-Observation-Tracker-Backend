package com.p532.tracker.decorator;

/**
 * DECORATOR PATTERN — BaseObservationProcessor
 *
 * The bottom-most link in the decorator chain.
 * Does nothing — simply returns the request as-is.
 * All decorators delegate to this eventually.
 */
public class BaseObservationProcessor implements ObservationProcessor {

    @Override
    public ObservationRequest process(ObservationRequest request) {
        return request;
    }
}
