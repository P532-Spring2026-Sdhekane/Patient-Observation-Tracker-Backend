package com.p532.tracker.decorator;

/**
 * DECORATOR PATTERN — ObservationProcessorDecorator
 *
 * Abstract base that holds a delegate field and forwards process()
 * to it by default. Concrete decorators override only the logic
 * they add, then call super.process() (or delegate.process() directly).
 *
 * Adding a new decorator in a future week = extend this class,
 * override process(), zero changes to existing decorators.
 */
public abstract class ObservationProcessorDecorator implements ObservationProcessor {

    protected final ObservationProcessor delegate;

    protected ObservationProcessorDecorator(ObservationProcessor delegate) {
        this.delegate = delegate;
    }

    @Override
    public ObservationRequest process(ObservationRequest request) {
        return delegate.process(request);
    }
}
