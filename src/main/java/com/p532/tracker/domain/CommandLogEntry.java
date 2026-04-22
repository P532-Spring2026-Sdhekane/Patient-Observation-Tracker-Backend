package com.p532.tracker.domain;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * Persistent record of every executed Command.
 *
 * Week 2 additions (Change 3):
 *  - userId: the user who executed the command (replaces hardcoded "staff")
 *  - undone: true once the command has been undone (prevents double-undo)
 */
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

    // ── Week 2 additions ──────────────────────────────────────────────────────
    @Column(nullable = false)
    private boolean undone = false;

    public CommandLogEntry() {}

    public CommandLogEntry(String commandType, String payload, Instant executedAt, String user) {
        this.commandType = commandType;
        this.payload     = payload;
        this.executedAt  = executedAt;
        this.user        = user;
    }

    public Long getId()            { return id; }
    public String getCommandType() { return commandType; }
    public String getPayload()     { return payload; }
    public Instant getExecutedAt() { return executedAt; }
    public String getUser()        { return user; }
    public void setUser(String u)  { this.user = u; }

    public boolean isUndone()      { return undone; }
    public void setUndone(boolean u) { this.undone = u; }
}
