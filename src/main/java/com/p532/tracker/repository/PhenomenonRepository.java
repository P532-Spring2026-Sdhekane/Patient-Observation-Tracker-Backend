package com.p532.tracker.repository;

import com.p532.tracker.domain.Phenomenon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PhenomenonRepository extends JpaRepository<Phenomenon, Long> {
    List<Phenomenon> findByPhenomenonType_Id(Long phenomenonTypeId);
}