package com.rehabai.api_gateway;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import java.util.stream.Collectors;

@Component
public class AuthHeadersFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // Pega o contexto de segurança (que foi preenchido pelo SecurityConfig)
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .filter(auth -> auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof Jwt)
                .map(auth -> {
                    Jwt jwt = (Jwt) auth.getPrincipal();

                    // Extrai as claims do JWT
                    String userId = jwt.getClaimAsString("user_id");
                    String email = jwt.getSubject();
                    String roles = auth.getAuthorities().stream()
                            .map(Object::toString)
                            .collect(Collectors.joining(","));

                    // Adiciona os novos cabeçalhos na requisição que vai para o microsserviço
                    ServerHttpRequest request = exchange.getRequest().mutate()
                            .header("X-User-Id", userId)
                            .header("X-User-Email", email)
                            .header("X-User-Roles", roles)
                            .build();

                    return exchange.mutate().request(request).build();
                })
                .defaultIfEmpty(exchange)
                .flatMap(chain::filter);
    }

    @Override
    public int getOrder() {
        // Executa DEPOIS do filtro de autenticação do Spring Security
        return -1;
    }
}