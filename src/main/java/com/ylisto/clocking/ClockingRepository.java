package com.ylisto.clocking;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

interface ClockingRepository extends JpaRepository<Clocking, UUID> {
}
