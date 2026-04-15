package com.p532.tracker.repository;

import com.p532.tracker.domain.AuditLogEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditLogEntryRepository extends JpaRepository<AuditLogEntry, Long> {
    List<AuditLogEntry> findAllByOrderByTimestampDesc();
}
