package com.ylisto.inventory;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

import java.util.UUID;
public record RegisterStockMovementRequest(@NotNull UUID variantId, @NotNull MovementType type, @NotNull BigDecimal quantity) {}
