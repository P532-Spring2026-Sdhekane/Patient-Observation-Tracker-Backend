package com.p532.tracker.repository;

import com.p532.tracker.domain.Protocol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProtocolRepository extends JpaRepository<Protocol, Long> {}
