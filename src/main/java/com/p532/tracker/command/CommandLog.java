package com.p532.tracker.command;

import com.p532.tracker.domain.CommandLogEntry;
import com.p532.tracker.repository.CommandLogEntryRepository;
import com.p532.tracker.repository.ObservationRepository;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * COMMAND PATTERN — CommandLog (Week 2 updated)
 *
 * Week 2 changes (Change 3):
 *  - Stores the actual username (not hardcoded "staff") in CommandLogEntry
 *  - Caches the TrackerCommand instance in memory (by entry id) to support undo
 *  - undo(entryId, username): validates the requesting user is the original executor,
 *    checks the command hasn't already been undone, calls command.undo(), marks entry
 *
 * Undo is single-level — a command that has been undone cannot be undone again.
 */
@Component
public class CommandLog {

    private final CommandLogEntryRepository logRepo;
    private final Clock                     clock;

    // In-memory cache: commandLogEntry.id -> TrackerCommand
    // Needed because JPA entities don't store the command object itself.
    private final Map<Long, TrackerCommand> commandCache = new ConcurrentHashMap<>();

    public CommandLog(CommandLogEntryRepository logRepo, Clock clock) {
        this.logRepo = logRepo;
        this.clock   = clock;
    }

    /**
     * Executes the command, persists a log entry, and caches the command for undo.
     *
     * @param command    the command to execute
     * @param actingUser the username performing the action
     * @return the result of command.execute()
     */
    public Object execute(TrackerCommand command, String actingUser) {
        Object result = command.execute();
        CommandLogEntry entry = new CommandLogEntry(
                command.getCommandType(),
                command.getPayloadJson(),
                Instant.now(clock),
                actingUser != null ? actingUser : "staff");
        CommandLogEntry saved = logRepo.save(entry);
        commandCache.put(saved.getId(), command);
        return result;
    }

    /** Week 1 / test compatibility — defaults actingUser to "staff" */
    public Object execute(TrackerCommand command) {
        return execute(command, "staff");
    }

    /**
     * Undoes a previously executed command.
     *
     * @param entryId      the CommandLogEntry id to undo
     * @param requestingUser the username requesting the undo
     * @return the result of command.undo()
     * @throws IllegalArgumentException  if entry not found or user mismatch
     * @throws IllegalStateException     if already undone
     * @throws UnsupportedOperationException if the command type doesn't support undo
     */
    public Object undo(Long entryId, String requestingUser) {
        CommandLogEntry entry = logRepo.findById(entryId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Command log entry not found: " + entryId));

        if (!entry.getUser().equals(requestingUser)) {
            throw new IllegalArgumentException(
                    "Only the original user '" + entry.getUser() + "' may undo this command");
        }

        if (entry.isUndone()) {
            throw new IllegalStateException("Command has already been undone");
        }

        TrackerCommand command = commandCache.get(entryId);
        if (command == null) {
            throw new IllegalStateException(
                    "Command is no longer in memory and cannot be undone (restart occurred)");
        }

        Object result = command.undo();
        entry.setUndone(true);
        logRepo.save(entry);
        commandCache.remove(entryId);
        return result;
    }
}
