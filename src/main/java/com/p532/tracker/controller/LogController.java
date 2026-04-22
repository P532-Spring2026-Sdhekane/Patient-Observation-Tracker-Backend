package com.p532.tracker.controller;

import com.p532.tracker.domain.AuditLogEntry;
import com.p532.tracker.domain.CommandLogEntry;
import com.p532.tracker.manager.CatalogueManager;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class LogController {

    private final CatalogueManager catalogueManager;

    public LogController(CatalogueManager catalogueManager) {
        this.catalogueManager = catalogueManager;
    }

    @GetMapping("/command-log")
    public List<CommandLogEntry> getCommandLog() {
        return catalogueManager.getCommandLog();
    }

    @GetMapping("/audit-log")
    public List<AuditLogEntry> getAuditLog() {
        return catalogueManager.getAuditLog();
    }
}
