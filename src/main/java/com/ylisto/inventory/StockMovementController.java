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
@RequestMapping("/movements")
public class StockMovementController {
    private final StockMovementService stockMovementService;

    public StockMovementController(StockMovementService stockMovementService) {
        this.stockMovementService = stockMovementService;
    }

    @PostMapping
    public ResponseEntity<StockMovementResponse> register(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody RegisterStockMovementRequest request) {
        UUID businessId = UUID.fromString(jwt.getClaimAsString("businessId"));
        UUID userId = UUID.fromString(jwt.getSubject());

        return ResponseEntity.status(HttpStatus.CREATED).body(StockMovementResponse.from(
                stockMovementService.register(businessId, userId, request.variantId(), request.type(), request.quantity())));
    }

    @GetMapping
    public ResponseEntity<List<StockMovementResponse>> list(@AuthenticationPrincipal Jwt jwt) {
        UUID businessId = UUID.fromString(jwt.getClaimAsString("businessId"));

        return ResponseEntity.ok(stockMovementService.findAll(businessId).stream().map(StockMovementResponse::from).toList());
    }
}
