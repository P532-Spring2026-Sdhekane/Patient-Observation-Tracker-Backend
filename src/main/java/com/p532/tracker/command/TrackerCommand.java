package com.p532.tracker.command;

/**
 * COMMAND PATTERN — TrackerCommand (Week 2 updated)
 *
 * Week 2 addition (Change 3): undo() method.
 * Undo semantics differ per command:
 *  - RecordObservationCommand.undo() → marks observation REJECTED
 *  - RejectObservationCommand.undo() → restores observation to ACTIVE
 *  - CreatePatientCommand.undo()     → throws UnsupportedOperationException
 */
public interface TrackerCommand {
    Object execute();
    Object undo();
    String getCommandType();
    String getPayloadJson();
}
