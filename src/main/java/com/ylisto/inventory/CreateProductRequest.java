package com.ylisto.inventory;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateProductRequest(@NotBlank String name, String imageUrl, @NotNull Unit unit) {
}
