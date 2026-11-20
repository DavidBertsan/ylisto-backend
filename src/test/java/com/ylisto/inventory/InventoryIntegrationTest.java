package com.ylisto.inventory;

import com.ylisto.TestcontainersConfiguration;
import com.ylisto.signup.SignupRequest;
import com.ylisto.signup.SignupResponse;
import com.ylisto.signup.SignupService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Real database: every inventory read and write stays inside the caller's business.
 */
@SpringBootTest
@Import(TestcontainersConfiguration.class)
class InventoryIntegrationTest {

	@Autowired
	private SignupService signupService;

	@Autowired
	private ProductService productService;

	@Autowired
	private ProductVariantService productVariantService;

	@Autowired
	private JdbcTemplate jdbc;

	@AfterEach
	void cleanDatabase() {
		jdbc.update("delete from product_variant");
		jdbc.update("delete from product");
		jdbc.update("delete from clocking");
		jdbc.update("delete from users");
		jdbc.update("delete from business");
	}

	@Test
	void shouldStoreProductWithVariantsAndExactDecimals() {
		SignupResponse butcher = signup("Carniceria Pepe", "pepe");
		Product chicken = productService.create(butcher.businessId(), new CreateProductRequest("Pollo", null, Unit.KILOGRAM));

		productVariantService.create(chicken.getId(), butcher.businessId(),
				"Pechuga", new BigDecimal("7.99"), new BigDecimal("12.500"));

		assertThat(productVariantService.findByProductId(chicken.getId(), butcher.businessId()))
				.singleElement()
				.satisfies(variant -> {
					assertThat(variant.getPrice()).isEqualByComparingTo("7.99");
					assertThat(variant.getQuantity()).isEqualByComparingTo("12.5");
				});
		String unit = jdbc.queryForObject("select unit from product where id = ?", String.class, chicken.getId());
		assertThat(unit).isEqualTo("KILOGRAM");   // stored as text, not as an ordinal
	}

	@Test
	void shouldOnlyListProductsOfItsOwnBusiness() {
		SignupResponse butcher = signup("Carniceria Pepe", "pepe");
		SignupResponse bar = signup("Bar La Esquina", "manolo");
		productService.create(butcher.businessId(), new CreateProductRequest("Pollo", null, Unit.KILOGRAM));

		assertThat(productService.findAll(bar.businessId())).isEmpty();
	}

	@Test
	void shouldNotFindOrExtendAProductOfAnotherBusiness() {
		SignupResponse butcher = signup("Carniceria Pepe", "pepe");
		SignupResponse bar = signup("Bar La Esquina", "manolo");
		Product chicken = productService.create(butcher.businessId(), new CreateProductRequest("Pollo", null, Unit.KILOGRAM));

		assertThatThrownBy(() -> productService.findById(chicken.getId(), bar.businessId()))
				.isInstanceOf(ProductNotFoundException.class);
		assertThatThrownBy(() -> productVariantService.create(chicken.getId(), bar.businessId(),
				"Pechuga", new BigDecimal("7.99"), BigDecimal.ONE))
				.isInstanceOf(ProductNotFoundException.class);
		assertThatThrownBy(() -> productVariantService.findByProductId(chicken.getId(), bar.businessId()))
				.isInstanceOf(ProductNotFoundException.class);
	}

	private SignupResponse signup(String businessName, String username) {
		return signupService.signup(new SignupRequest(businessName, "Some type", username,
				"a-long-password", "Owner", "600000000", null));
	}
}
