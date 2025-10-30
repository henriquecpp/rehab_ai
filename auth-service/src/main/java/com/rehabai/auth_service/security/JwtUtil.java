package com.rehabai.auth_service.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.Jwt;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.SecretKey;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;

@Component
public class JwtUtil {

    private final NimbusJwtEncoder encoder;
    private final NimbusJwtDecoder decoder;
    private final long expirationMs;

    public JwtUtil(@Value("${auth.jwt.secret}") String secret,
                   @Value("${auth.jwt.expiration-ms}") long expirationMs) {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        SecretKey secretKey = new SecretKeySpec(keyBytes, "HmacSHA256");
        this.encoder = new NimbusJwtEncoder(new ImmutableSecret<>(secretKey));
        this.decoder = NimbusJwtDecoder.withSecretKey(secretKey).macAlgorithm(org.springframework.security.oauth2.jose.jws.MacAlgorithm.HS256).build();
        this.expirationMs = expirationMs;
    }

    public String generateToken(UserDetails userDetails) {
        return generateToken(userDetails, Map.of());
    }

    public String generateToken(UserDetails userDetails, Map<String, Object> extraClaims) {
        Instant now = Instant.now();
        Instant exp = now.plusMillis(expirationMs);

        List<String> roles = userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList());

        JwtClaimsSet.Builder builder = JwtClaimsSet.builder()
                .subject(userDetails.getUsername())
                .issuedAt(now)
                .expiresAt(exp)
                .claim("roles", roles);

        if (extraClaims != null) {
            extraClaims.forEach(builder::claim);
        }

        JwtClaimsSet claims = builder.build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        JwtEncoderParameters encoderParameters = JwtEncoderParameters.from(header, claims);
        return this.encoder.encode(encoderParameters).getTokenValue();
    }

    public boolean validateToken(String token) {
        try {
            Jwt jwt = decoder.decode(token);
            // Optionally validate exp/nbf automatically thrown by decoder
            return jwt.getExpiresAt() == null || jwt.getExpiresAt().isAfter(Instant.now());
        } catch (Exception ex) {
            return false;
        }
    }

    public String getUsernameFromToken(String token) {
        Jwt jwt = decoder.decode(token);
        return jwt.getSubject();
    }

    @SuppressWarnings("unchecked")
    public List<String> getRolesFromToken(String token) {
        Jwt jwt = decoder.decode(token);
        Object rolesObj = jwt.getClaims().get("roles");
        if (rolesObj instanceof List) {
            return ((List<?>) rolesObj).stream().map(Object::toString).collect(Collectors.toList());
        }
        if (rolesObj instanceof String) {
            return List.of((String) rolesObj);
        }
        return List.of();
    }

    public long getExpirationMs() {
        return expirationMs;
    }
}
