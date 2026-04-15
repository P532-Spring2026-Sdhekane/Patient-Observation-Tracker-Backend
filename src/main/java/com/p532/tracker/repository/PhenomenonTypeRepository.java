package com.p532.tracker.repository;

import com.p532.tracker.domain.PhenomenonType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PhenomenonTypeRepository extends JpaRepository<PhenomenonType, Long> {}
