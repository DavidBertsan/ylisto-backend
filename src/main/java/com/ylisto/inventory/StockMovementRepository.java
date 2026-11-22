package com.ylisto.inventory;


import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

interface StockMovementRepository extends JpaRepository<StockMovement, UUID> {
    List<StockMovement> findAllByVariantProductBusinessIdOrderByOccurredAtDesc(UUID businessId);

}
