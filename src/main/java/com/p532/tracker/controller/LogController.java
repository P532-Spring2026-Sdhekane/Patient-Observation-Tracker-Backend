package com.p532.tracker.controller;

import com.p532.tracker.command.CommandLog;
import com.p532.tracker.domain.AuditLogEntry;
import com.p532.tracker.domain.CommandLogEntry;
import com.p532.tracker.manager.CatalogueManager;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * CLIENT LAYER — LogController (Week 2 updated)
 *
 * Week 2 addition (Change 3):
 *   POST /api/command-log/{id}/undo  — triggers undo of a specific command.
 *   The requesting username is passed in the request body.
 *   Only the original user who executed the command may undo it.
 */
@RestController
@RequestMapping("/api")
public class LogController {

    private final CatalogueManager catalogueManager;
    private final CommandLog       commandLog;

    public LogController(CatalogueManager catalogueManager, CommandLog commandLog) {
        this.catalogueManager = catalogueManager;
        this.commandLog       = commandLog;
    }

    @GetMapping("/command-log")
    public List<CommandLogEntry> getCommandLog() {
        return catalogueManager.getCommandLog();
    }

    @GetMapping("/audit-log")
    public List<AuditLogEntry> getAuditLog() {
        return catalogueManager.getAuditLog();
    }

    @PostMapping("/command-log/{id}/undo")
    public ResponseEntity<?> undoCommand(@PathVariable Long id,
                                          @RequestBody Map<String, String> body) {
        try {
            String requestingUser = body.getOrDefault("username", "staff");
            Object result = commandLog.undo(id, requestingUser);
            return ResponseEntity.ok(Map.of(
                    "message", "Command " + id + " undone successfully",
                    "result",  result != null ? result.toString() : "ok"));
        } catch (UnsupportedOperationException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "This command type does not support undo: " + e.getMessage()));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
