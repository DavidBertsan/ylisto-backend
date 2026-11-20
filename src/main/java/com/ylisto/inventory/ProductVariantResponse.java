package com.ylisto.inventory;


import java.math.BigDecimal;
import java.util.UUID;

public record ProductVariantResponse(UUID id, UUID productId, String label, BigDecimal price, BigDecimal quantity){
    public static ProductVariantResponse from(ProductVariant productVariant) {
        return new ProductVariantResponse(productVariant.getId(),productVariant.getProduct().getId(), productVariant.getLabel(),productVariant.getPrice(),productVariant.getQuantity());
    }
}
