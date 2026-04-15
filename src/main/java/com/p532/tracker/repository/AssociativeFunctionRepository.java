package com.p532.tracker.repository;

import com.p532.tracker.domain.AssociativeFunction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssociativeFunctionRepository extends JpaRepository<AssociativeFunction, Long> {
    List<AssociativeFunction> findByActiveTrue();
}
