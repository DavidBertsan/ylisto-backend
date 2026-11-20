package com.ylisto.inventory;

import com.ylisto.business.Business;
import com.ylisto.security.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
@Import(SecurityConfig.class)
class ProductControllerTest {

	private static final String PRODUCT_BODY = """
			{ "name": "Pollo", "unit": "KILOGRAM" }
			""";

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private ProductService productService;

	@MockitoBean
	private ProductVariantService productVariantService;

	private final UUID businessId = UUID.randomUUID();
	private final Product chicken = new Product(new Business("Carniceria Pepe", "Butcher"), "Pollo", null, Unit.KILOGRAM);

	@Test
	void shouldLetAnEmployeeReadTheProducts() throws Exception {
		when(productService.findAll(businessId)).thenReturn(List.of(chicken));

		mockMvc.perform(get("/products").with(token("EMPLOYEE")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].name").value("Pollo"))
				.andExpect(jsonPath("$[0].unit").value("KILOGRAM"));
	}

	@Test
	void shouldReturn403WhenEmployeeCreatesAProduct() throws Exception {
		mockMvc.perform(post("/products").with(token("EMPLOYEE"))
						.contentType(MediaType.APPLICATION_JSON).content(PRODUCT_BODY))
				.andExpect(status().isForbidden());

		verify(productService, never()).create(any(), any());
	}

	@Test
	void shouldReturn201WhenAdminCreatesAProduct() throws Exception {
		when(productService.create(eq(businessId), any())).thenReturn(chicken);

		mockMvc.perform(post("/products").with(token("ADMIN"))
						.contentType(MediaType.APPLICATION_JSON).content(PRODUCT_BODY))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.name").value("Pollo"));
	}

	@Test
	void shouldReturn400WhenUnitIsMissing() throws Exception {
		mockMvc.perform(post("/products").with(token("ADMIN"))
						.contentType(MediaType.APPLICATION_JSON).content("""
								{ "name": "Pollo" }
								"""))
				.andExpect(status().isBadRequest());
	}

	@Test
	void shouldReturn404WhenProductIsNotInYourBusiness() throws Exception {
		UUID productId = UUID.randomUUID();
		when(productService.findById(productId, businessId))
				.thenThrow(new ProductNotFoundException("Product not found: " + productId));

		mockMvc.perform(get("/products/{productId}", productId).with(token("EMPLOYEE")))
				.andExpect(status().isNotFound());
	}

	@Test
	void shouldReturn201WhenAdminAddsAVariant() throws Exception {
		UUID productId = UUID.randomUUID();
		when(productVariantService.create(eq(productId), eq(businessId), eq("Pechuga"), any(), any()))
				.thenReturn(new ProductVariant(chicken, "Pechuga", new BigDecimal("7.99"), new BigDecimal("12.500")));

		mockMvc.perform(post("/products/{productId}/variants", productId).with(token("ADMIN"))
						.contentType(MediaType.APPLICATION_JSON).content("""
								{ "label": "Pechuga", "price": 7.99, "quantity": 12.5 }
								"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.label").value("Pechuga"))
				.andExpect(jsonPath("$.price").value(7.99));
	}

	@Test
	void shouldReturn400WhenVariantPriceIsNegative() throws Exception {
		mockMvc.perform(post("/products/{productId}/variants", UUID.randomUUID()).with(token("ADMIN"))
						.contentType(MediaType.APPLICATION_JSON).content("""
								{ "label": "Pechuga", "price": -1, "quantity": 12.5 }
								"""))
				.andExpect(status().isBadRequest());

		verify(productVariantService, never()).create(any(), any(), any(), any(), any());
	}

	private RequestPostProcessor token(String role) {
		return jwt()
				.jwt(t -> t.subject(UUID.randomUUID().toString()).claim("businessId", businessId.toString()).claim("role", role))
				.authorities(new SimpleGrantedAuthority("ROLE_" + role));
	}
}
