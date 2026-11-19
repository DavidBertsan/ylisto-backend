package com.ylisto.security;

import com.ylisto.user.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

/**
 * Issues signed JWTs. Claims:
 * - sub        → user id (read it in controllers with jwt.getSubject())
 * - businessId → tenant of the user (jwt.getClaimAsString("businessId"))
 * - role       → ADMIN / EMPLOYEE
 */
@Service
public class TokenService {

	private final JwtEncoder jwtEncoder;
	private final Clock clock;
	private final Duration ttl;

	public TokenService(JwtEncoder jwtEncoder, Clock clock, @Value("${ylisto.jwt.ttl}") Duration ttl) {
		this.jwtEncoder = jwtEncoder;
		this.clock = clock;
		this.ttl = ttl;
	}

	public String issue(User user) {
		Instant now = Instant.now(clock);
		JwtClaimsSet claims = JwtClaimsSet.builder()
				.issuer("ylisto")
				.issuedAt(now)
				.expiresAt(now.plus(ttl))
				.subject(user.getId().toString())
				.claim("businessId", user.getBusiness().getId().toString())
				.claim("role", user.getRole().name())
				.build();
		JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
		return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
	}
}
