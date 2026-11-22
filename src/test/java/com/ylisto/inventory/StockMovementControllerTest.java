package com.ylisto.inventory;

import com.ylisto.business.Business;
import com.ylisto.security.SecurityConfig;
import com.ylisto.user.Role;
import com.ylisto.user.User;
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
import java.time.Instant;
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

@WebMvcTest(StockMovementController.class)
@Import(SecurityConfig.class)
class StockMovementControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private StockMovementService stockMovementService;

	private final UUID businessId = UUID.randomUUID();
	private final UUID userId = UUID.randomUUID();
	private final UUID variantId = UUID.randomUUID();

	private final Business butcher = new Business("Carniceria Pepe", "Butcher");
	private final ProductVariant breast = new ProductVariant(
			new Product(butcher, "Pollo", null, Unit.KILOGRAM), "Pechuga", new BigDecimal("7.99"), new BigDecimal("10"));
	private final User employee = new User("ana", "hash", butcher, "Ana", "600000000", Role.EMPLOYEE, null);
	private final StockMovement sale = new StockMovement(
			breast, MovementType.SALE, new BigDecimal("-2"), employee, Instant.parse("2026-11-22T10:00:00Z"));

	@Test
	void shouldLetAnEmployeeRegisterASaleWithIdsTakenFromTheToken() throws Exception {
		when(stockMovementService.register(businessId, userId, variantId, MovementType.SALE, new BigDecimal("2")))
				.thenReturn(sale);

		mockMvc.perform(post("/movements").with(token("EMPLOYEE"))
						.contentType(MediaType.APPLICATION_JSON).content(body("SALE", "2")))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.type").value("SALE"))
				.andExpect(jsonPath("$.quantityChange").value(-2));
	}

	@Test
	void shouldReturn400WhenQuantityIsMissing() throws Exception {
		mockMvc.perform(post("/movements").with(token("EMPLOYEE"))
						.contentType(MediaType.APPLICATION_JSON).content("""
								{ "variantId": "%s", "type": "SALE" }
								""".formatted(variantId)))
				.andExpect(status().isBadRequest());

		verify(stockMovementService, never()).register(any(), any(), any(), any(), any());
	}

	@Test
	void shouldReturn409WhenThereIsNotEnoughStock() throws Exception {
		when(stockMovementService.register(eq(businessId), eq(userId), eq(variantId), eq(MovementType.SALE), any()))
				.thenThrow(new InsufficientStockException("Insufficient stock for Pechuga: available 10"));

		mockMvc.perform(post("/movements").with(token("EMPLOYEE"))
						.contentType(MediaType.APPLICATION_JSON).content(body("SALE", "50")))
				.andExpect(status().isConflict());
	}

	@Test
	void shouldReturn403WhenEmployeeReadsTheMovementLog() throws Exception {
		mockMvc.perform(get("/movements").with(token("EMPLOYEE")))
				.andExpect(status().isForbidden());

		verify(stockMovementService, never()).findAll(any());
	}

	@Test
	void shouldLetAdminReadTheMovementLog() throws Exception {
		when(stockMovementService.findAll(businessId)).thenReturn(List.of(sale));

		mockMvc.perform(get("/movements").with(token("ADMIN")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].type").value("SALE"))
				.andExpect(jsonPath("$[0].quantityChange").value(-2));
	}

	private String body(String type, String quantity) {
		return """
				{ "variantId": "%s", "type": "%s", "quantity": %s }
				""".formatted(variantId, type, quantity);
	}

	private RequestPostProcessor token(String role) {
		return jwt()
				.jwt(t -> t.subject(userId.toString()).claim("businessId", businessId.toString()).claim("role", role))
				.authorities(new SimpleGrantedAuthority("ROLE_" + role));
	}
}
