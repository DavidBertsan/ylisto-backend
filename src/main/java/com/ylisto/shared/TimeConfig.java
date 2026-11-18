package com.ylisto.shared;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/**
 * Inject this Clock and call Instant.now(clock) instead of Instant.now(),
 * so tests can freeze time with Clock.fixed(...).
 */
@Configuration
public class TimeConfig {

	@Bean
	Clock clock() {
		return Clock.systemUTC();
	}
}
