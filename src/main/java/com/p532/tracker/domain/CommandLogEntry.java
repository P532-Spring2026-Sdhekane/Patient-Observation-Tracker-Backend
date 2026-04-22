package com.p532.tracker.domain;

import jakarta.persistence.*;
import java.time.Instant;


@Entity
@Table(name = "command_log_entries")
public class CommandLogEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String commandType;

    @Column(columnDefinition = "TEXT")
    private String payload;

    @Column(nullable = false)
    private Instant executedAt;

    @Column(nullable = false)
    private String user;

    public CommandLogEntry() {}

    public CommandLogEntry(String commandType, String payload, Instant executedAt, String user) {
        this.commandType = commandType;
        this.payload = payload;
        this.executedAt = executedAt;
        this.user = user;
    }

    public Long getId() { return id; }
    public String getCommandType() { return commandType; }
    public String getPayload() { return payload; }
    public Instant getExecutedAt() { return executedAt; }
    public String getUser() { return user; }
}
