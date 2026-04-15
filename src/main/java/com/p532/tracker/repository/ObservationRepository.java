package com.p532.tracker.repository;

import com.p532.tracker.domain.Observation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ObservationRepository extends JpaRepository<Observation, Long> {
    List<Observation> findByPatientIdOrderByRecordingTimeDesc(Long patientId);
}
