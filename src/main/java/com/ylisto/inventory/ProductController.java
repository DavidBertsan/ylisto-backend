package com.ylisto.inventory;


import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;


import java.util.List;
import java.util.UUID;


@RestController
@RequestMapping("/products")
public class ProductController {
    private final ProductService productService;
    private final ProductVariantService productVariantService;

    public ProductController(ProductService productService, ProductVariantService productVariantService) {
        this.productService = productService;
        this.productVariantService = productVariantService;
    }

    @GetMapping
    public ResponseEntity<List<ProductResponse>> findProducts(@AuthenticationPrincipal Jwt jwt){
        UUID businessId = UUID.fromString(jwt.getClaimAsString("businessId"));
        return ResponseEntity.ok(productService.findAll(businessId).stream().map(ProductResponse::from).toList());
    }

    @PostMapping
    public ResponseEntity<ProductResponse> create(@AuthenticationPrincipal Jwt jwt,@Valid @RequestBody CreateProductRequest request){
        UUID businessId = UUID.fromString(jwt.getClaimAsString("businessId"));
        Product saved =productService.create(businessId,request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ProductResponse.from(saved));
    }



    @GetMapping("/{productId}")
    public ResponseEntity<ProductResponse> findById(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID productId) {
        UUID businessId = UUID.fromString(jwt.getClaimAsString("businessId"));
        return ResponseEntity.ok(ProductResponse.from(productService.findById(productId, businessId)));
    }

    //Usamos el Mismo controller de Product y productVariant

    @PostMapping("/{productId}/variants")
    public ResponseEntity<ProductVariantResponse> createVariant(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID productId, @Valid @RequestBody CreateProductVariantRequest request) {
        UUID businessId = UUID.fromString(jwt.getClaimAsString("businessId"));
        ProductVariant saved = productVariantService.create(productId, businessId,
                request.label(), request.price(), request.quantity());
        return ResponseEntity.status(HttpStatus.CREATED).body(ProductVariantResponse.from(saved));
    }

    @GetMapping("/{productId}/variants")
    public ResponseEntity<List<ProductVariantResponse>> findVariants(@AuthenticationPrincipal Jwt jwt,
                                                                     @PathVariable UUID productId) {
        UUID businessId = UUID.fromString(jwt.getClaimAsString("businessId"));
        return ResponseEntity.ok(productVariantService.findByProductId(productId, businessId).stream()
                .map(ProductVariantResponse::from)
                .toList());
    }



}
