package com.ylisto.inventory;



import java.util.UUID;

public record ProductResponse(UUID id, String name, String imageUrl, Unit unit) {
    public static ProductResponse from(Product product) {
        return new ProductResponse( product.getId(), product.getName(), product.getImageUrl(), product.getUnit());
    }

}