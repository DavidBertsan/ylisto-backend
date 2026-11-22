package com.ylisto.inventory;


import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record StockMovementResponse(UUID id, UUID variantId, MovementType type, BigDecimal quantityChange, UUID userId,Instant occurredAt) {
    public static StockMovementResponse from(StockMovement stockMovement){
        return new StockMovementResponse(stockMovement.getId(),stockMovement.getVariant().getId(),stockMovement.getType(),stockMovement.getQuantityChange(),stockMovement.getUser().getId(),stockMovement.getOccurredAt());
    }
}
