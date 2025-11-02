package com.rehabai.api_gateway;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

/**
 * WebFlux filter that logs the presence of the Authorization header.
 * It does not mutate the header; it only ensures it is available downstream.
 */
//@Component // deliberadamente não registrado: oauth2-resource-server já extrai/valida o Bearer token
public class AuthorizationHeaderFilter implements WebFilter {

    private static final Logger log = LoggerFactory.getLogger(AuthorizationHeaderFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String auth = exchange.getRequest().getHeaders().getFirst("Authorization");
        if (auth != null && !auth.isEmpty()) {
            log.debug("Authorization header present, length={}", auth.length());
        }
        return chain.filter(exchange);
    }
}
