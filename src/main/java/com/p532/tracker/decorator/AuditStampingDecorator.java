package com.p532.tracker.decorator;

import java.time.Clock;
import java.time.Instant;

/**
 * DECORATOR PATTERN — AuditStampingDecorator
 *
 * Outermost decorator. Stamps the recording timestamp (from the
 * injected Clock) and the acting user onto the request before
 * passing it down the chain.
 *
 * Must run first (outermost) so downstream decorators can read
 * recordingTime if needed.
 */
public class AuditStampingDecorator extends ObservationProcessorDecorator {

    private final Clock  clock;
    private final String actingUser;

    public AuditStampingDecorator(ObservationProcessor delegate, Clock clock, String actingUser) {
        super(delegate);
        this.clock      = clock;
        this.actingUser = actingUser;
    }

    @Override
    public ObservationRequest process(ObservationRequest request) {
        // Stamp before passing down the chain
        request.setRecordingTime(Instant.now(clock));
        request.setActingUser(actingUser);
        return delegate.process(request);
    }
}
