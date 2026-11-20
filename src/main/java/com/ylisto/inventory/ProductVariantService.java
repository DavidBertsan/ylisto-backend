package com.ylisto.inventory;

import com.ylisto.business.Business;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class ProductVariantService {
    private final ProductVariantRepository productVariantRepository;
    private final ProductService productService;
    public ProductVariantService(ProductVariantRepository productVariantRepository, ProductService productService) {
        this.productVariantRepository = productVariantRepository;
        this.productService = productService;
    }

    public ProductVariant create(UUID productId, UUID businessId, String label, BigDecimal price, BigDecimal quantity) {
        Product product=productService.findById(productId,businessId);
        ProductVariant productVariant=new ProductVariant(product,label,price,quantity);
        return productVariantRepository.save(productVariant);
    }

    public List<ProductVariant> findByProductId(UUID productId,UUID businessId){
        productService.findById(productId,businessId);
        return productVariantRepository.findByProductId(productId);

    }

}
