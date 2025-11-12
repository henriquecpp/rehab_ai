package com.rehabai.api_gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.stream.Collectors;


@Component
public class JwtToHeadersFilter implements WebFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtToHeadersFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        log.debug("JwtToHeadersFilter: Processing request to {}", exchange.getRequest().getURI());

        return ReactiveSecurityContextHolder.getContext()
            .map(SecurityContext::getAuthentication)
            .filter(Authentication::isAuthenticated)
            .flatMap(authentication -> {
                /* log.debug("Authentication found: {}, Principal type: {}",
                    authentication.getName(),
                    authentication.getPrincipal().getClass().getSimpleName()); */

                if (authentication.getPrincipal() instanceof Jwt jwt) {
                    String userId = extractUserId(jwt);
                    String roles = extractRoles(authentication);
                    String email = jwt.getClaimAsString("email");

                    /* log.info("JWT → Headers: userId={}, roles={}, email={}", userId, roles, email);
                    log.debug("JWT claims: {}", jwt.getClaims());
                    log.debug("Authorities: {}", authentication.getAuthorities()); */

                    ServerWebExchange mutatedExchange = exchange.mutate()
                        .request(builder -> {
                            builder.headers(headers -> headers.remove("Authorization"));

                            if (userId != null) {
                                builder.header("X-User-Id", userId);
                                //log.debug("Added header: X-User-Id={}", userId);
                            }
                            if (roles != null && !roles.isEmpty()) {
                                builder.header("X-User-Roles", roles);
                                //log.debug("Added header: X-User-Roles={}", roles);
                            } else {
                                //log.warn("No roles found to add to X-User-Roles header!");
                            }
                            if (email != null) {
                                builder.header("X-User-Email", email);
                                //log.debug("Added header: X-User-Email={}", email);
                            }
                        })
                        .build();

                    return chain.filter(mutatedExchange);
                }

                //log.warn("Principal is not a JWT! Type: {}", authentication.getPrincipal().getClass());
                return chain.filter(exchange);
            })
            .switchIfEmpty(Mono.defer(() -> {
                //log.warn("No authentication found in SecurityContext for request to {}", exchange.getRequest().getURI());
                return chain.filter(exchange);
            }));
    }

    private String extractUserId(Jwt jwt) {
        Object userIdClaim = jwt.getClaim("user_id");
        return userIdClaim != null ? userIdClaim.toString() : null;
    }

    private String extractRoles(Authentication authentication) {
        return authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.joining(","));
    }
}

