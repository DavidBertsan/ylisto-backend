package com.ylisto.clocking;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

interface ClockingRepository extends JpaRepository<Clocking, UUID> {
    Optional<Clocking> findByUserIdAndClockOutIsNull(UUID userId);

}
