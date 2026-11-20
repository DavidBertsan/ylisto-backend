package com.ylisto.inventory;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;


public record CreateProductVariantRequest(@NotBlank String label, @NotNull @Positive BigDecimal price, @NotNull @PositiveOrZero BigDecimal quantity){}
