package com.p532.tracker.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/**
 * Provides a system UTC Clock bean.
 * Injecting Clock (rather than calling Instant.now() directly) keeps
 * tests deterministic — tests can supply a fixed Clock instead.
 */
@Configuration
public class ClockConfig {

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }
}
