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
        return ReactiveSecurityContextHolder.getContext()
            .map(SecurityContext::getAuthentication)
            .filter(Authentication::isAuthenticated)
            .flatMap(authentication -> {
                if (authentication.getPrincipal() instanceof Jwt jwt) {
                    String userId = extractUserId(jwt);
                    String roles = extractRoles(authentication);
                    String email = jwt.getClaimAsString("email");

                    ServerWebExchange mutatedExchange = exchange.mutate()
                        .request(builder -> {
                            builder.headers(headers -> headers.remove("Authorization"));

                            if (userId != null) {
                                builder.header("X-User-Id", userId);
                            }
                            if (roles != null) {
                                builder.header("X-User-Roles", roles);
                            }
                            if (email != null) {
                                builder.header("X-User-Email", email);
                            }
                        })
                        .build();

                    log.debug("JWT → Headers: userId={}, roles={}, email={}", userId, roles, email);

                    return chain.filter(mutatedExchange);
                }

                return chain.filter(exchange);
            })
            .switchIfEmpty(chain.filter(exchange));
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

