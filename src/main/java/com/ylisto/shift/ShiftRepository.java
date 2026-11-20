package com.ylisto.shift;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

interface ShiftRepository extends JpaRepository<Shift, UUID> {
    boolean existsByUserIdAndStartsAtLessThanAndEndsAtGreaterThan(UUID userId, Instant newEnd, Instant newStart);
    List<Shift> findAllByUserIdAndStartsAtBetweenOrderByStartsAt(UUID userId, Instant from, Instant to);
    Optional<Shift> findByIdAndUserBusinessId(UUID shiftId, UUID businessId);

}
