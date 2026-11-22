package com.ylisto.inventory;


import com.ylisto.user.User;
import com.ylisto.user.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class StockMovementService {
    private final StockMovementRepository stockMovementRepository;
    private final ProductVariantRepository productVariantRepository;
    private final UserService userService;
    private final Clock clock;

    public StockMovementService(StockMovementRepository stockMovementRepository, ProductVariantRepository productVariantRepository, UserService userService, Clock clock) {
        this.stockMovementRepository = stockMovementRepository;
        this.productVariantRepository = productVariantRepository;
        this.userService = userService;
        this.clock = clock;
    }
    @Transactional
    public StockMovement register(UUID businessId, UUID userId, UUID variantId, MovementType type, BigDecimal quantity) {
        if (quantity.signum() == 0 || (type != MovementType.ADJUSTMENT && quantity.signum() < 0)) {
            throw new InvalidMovementException("Quantity must be positive (or non-zero for adjustments)");
        }
        ProductVariant variant=productVariantRepository.findByIdAndProductBusinessId(variantId, businessId) .orElseThrow(() -> new ProductNotFoundException("Variant not found: " + variantId));

        BigDecimal change = switch (type) {
            case SALE -> quantity.negate();
            case RESTOCK, ADJUSTMENT -> quantity;
        };

        variant.applyMovement(change);
        User user= userService.findById(userId);

        return stockMovementRepository.save(new StockMovement(variant, type, change, user,Instant.now(clock)));

    }
    public List<StockMovement> findAll(UUID businessId) {
        return stockMovementRepository.findAllByVariantProductBusinessIdOrderByOccurredAtDesc(businessId);
    }

}
