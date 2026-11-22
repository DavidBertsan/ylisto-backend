package com.ylisto.inventory;

import com.ylisto.TestcontainersConfiguration;
import com.ylisto.signup.SignupRequest;
import com.ylisto.signup.SignupResponse;
import com.ylisto.signup.SignupService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Real database: a movement and its stock change are saved together or not at all,
 * and @Version stops two writers from silently overwriting each other.
 */
@SpringBootTest
@Import(TestcontainersConfiguration.class)
class StockMovementIntegrationTest {

	@Autowired
	private SignupService signupService;

	@Autowired
	private ProductService productService;

	@Autowired
	private ProductVariantService productVariantService;

	@Autowired
	private StockMovementService stockMovementService;

	@Autowired
	private ProductVariantRepository productVariantRepository;

	@Autowired
	private JdbcTemplate jdbc;

	private SignupResponse butcher;
	private ProductVariant breast;

	@BeforeEach
	void createStock() {
		butcher = signup("Carniceria Pepe", "pepe");
		Product chicken = productService.create(butcher.businessId(), new CreateProductRequest("Pollo", null, Unit.KILOGRAM));
		breast = productVariantService.create(chicken.getId(), butcher.businessId(),
				"Pechuga", new BigDecimal("7.99"), new BigDecimal("10.000"));
	}

	@AfterEach
	void cleanDatabase() {
		jdbc.update("delete from stock_movement");
		jdbc.update("delete from product_variant");
		jdbc.update("delete from product");
		jdbc.update("delete from clocking");
		jdbc.update("delete from users");
		jdbc.update("delete from business");
	}

	@Test
	void shouldSubtractASaleAndAddARestock() {
		sell("2.500");
		stockMovementService.register(butcher.businessId(), butcher.userId(), breast.getId(), MovementType.RESTOCK, new BigDecimal("5"));

		assertThat(stockOfBreast()).isEqualByComparingTo("12.5");
		assertThat(stockMovementService.findAll(butcher.businessId()))
				.extracting(StockMovement::getQuantityChange)
				.usingElementComparator(BigDecimal::compareTo)
				.containsExactly(new BigDecimal("5"), new BigDecimal("-2.5"));   // newest first
	}

	@Test
	void shouldRejectASaleBiggerThanTheStockAndSaveNothing() {
		assertThatThrownBy(() -> sell("11"))
				.isInstanceOf(InsufficientStockException.class);

		assertThat(stockOfBreast()).isEqualByComparingTo("10");
		assertThat(stockMovementService.findAll(butcher.businessId())).isEmpty();
	}

	@Test
	void shouldNotMoveStockOfAnotherBusiness() {
		SignupResponse bar = signup("Bar La Esquina", "manolo");

		assertThatThrownBy(() -> stockMovementService.register(bar.businessId(), bar.userId(), breast.getId(), MovementType.SALE, BigDecimal.ONE))
				.isInstanceOf(ProductNotFoundException.class);
		assertThat(stockOfBreast()).isEqualByComparingTo("10");
	}

	@Test
	void shouldRejectAStaleWriteThanksToVersion() {
		// Someone loads the variant, then another sale lands before they save
		ProductVariant staleCopy = productVariantRepository.findById(breast.getId()).orElseThrow();
		sell("3");

		staleCopy.applyMovement(new BigDecimal("-1"));

		// Without @Version this would silently overwrite 7 with 9 and lose the first sale
		assertThatThrownBy(() -> productVariantRepository.save(staleCopy))
				.isInstanceOf(ObjectOptimisticLockingFailureException.class);
		assertThat(stockOfBreast()).isEqualByComparingTo("7");
	}

	private void sell(String quantity) {
		stockMovementService.register(butcher.businessId(), butcher.userId(), breast.getId(), MovementType.SALE, new BigDecimal(quantity));
	}

	private BigDecimal stockOfBreast() {
		return jdbc.queryForObject("select quantity from product_variant where id = ?", BigDecimal.class, breast.getId());
	}

	private SignupResponse signup(String businessName, String username) {
		return signupService.signup(new SignupRequest(businessName, "Some type", username,
				"a-long-password", "Owner", "600000000", null));
	}
}
