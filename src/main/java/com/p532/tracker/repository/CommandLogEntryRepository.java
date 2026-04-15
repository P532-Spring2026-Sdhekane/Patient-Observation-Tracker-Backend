package com.p532.tracker.repository;

import com.p532.tracker.domain.CommandLogEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommandLogEntryRepository extends JpaRepository<CommandLogEntry, Long> {
    List<CommandLogEntry> findAllByOrderByExecutedAtDesc();
}
