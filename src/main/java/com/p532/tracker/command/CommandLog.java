package com.p532.tracker.command;

import com.p532.tracker.domain.CommandLogEntry;
import com.p532.tracker.repository.CommandLogEntryRepository;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;

@Component
public class CommandLog {

    private static final String ACTING_USER = "staff";

    private final CommandLogEntryRepository logRepo;
    private final Clock clock;

    public CommandLog(CommandLogEntryRepository logRepo, Clock clock) {
        this.logRepo = logRepo;
        this.clock = clock;
    }

    /**
     * Executes the command and records it in the persistent command log.
     *
     * @param command the command to execute
     * @return the result returned by command.execute()
     */
    public Object execute(TrackerCommand command) {
        Object result = command.execute();
        CommandLogEntry entry = new CommandLogEntry(
                command.getCommandType(),
                command.getPayloadJson(),
                Instant.now(clock),
                ACTING_USER
        );
        logRepo.save(entry);
        return result;
    }
}
