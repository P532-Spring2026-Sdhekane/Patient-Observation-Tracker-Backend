package com.p532.tracker.config;

import com.p532.tracker.domain.StrategyType;
import com.p532.tracker.engine.DiagnosisStrategy;
import com.p532.tracker.engine.SimpleConjunctiveStrategy;
import com.p532.tracker.engine.WeightedScoringStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

/**
 * Provides the Map<StrategyType, DiagnosisStrategy> bean that
 * DiagnosisEngine uses to look up the correct strategy per rule.
 *
 * Adding a new strategy in Week 3+ = add one entry here, nothing else changes.
 */
@Configuration
public class StrategyConfig {

    @Bean
    public Map<StrategyType, DiagnosisStrategy> diagnosisStrategies(
            SimpleConjunctiveStrategy conjunctive,
            WeightedScoringStrategy weighted) {
        return Map.of(
                StrategyType.CONJUNCTIVE, conjunctive,
                StrategyType.WEIGHTED,    weighted
        );
    }
}
