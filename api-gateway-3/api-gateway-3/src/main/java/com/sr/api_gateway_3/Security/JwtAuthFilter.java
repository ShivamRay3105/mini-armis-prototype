// package com.sr.api_gateway_3.Security;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.http.HttpStatus;
// import org.springframework.http.server.reactive.ServerHttpRequest;
// import org.springframework.stereotype.Component;
// import org.springframework.web.server.ServerWebExchange;
// import org.springframework.cloud.gateway.filter.GatewayFilter;
// import org.springframework.cloud.gateway.filter.GatewayFilterChain;
// import reactor.core.publisher.Mono;

// @Component
// public class JwtAuthFilter implements GatewayFilter {

// @Autowired
// private JwtService jwtService;

// @Override
// public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain
// chain) {
// ServerHttpRequest request = exchange.getRequest();

// // Allow login/register endpoints without token
// if (request.getURI().getPath().contains("/auth/login") ||
// request.getURI().getPath().contains("/auth/register")) {
// return chain.filter(exchange);
// }

// // Check Authorization header
// if (!request.getHeaders().containsKey("Authorization")) {
// exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
// return exchange.getResponse().setComplete();
// }

// String authHeader = request.getHeaders().getFirst("Authorization");
// String token = authHeader.replace("Bearer ", "");

// try {
// String username = jwtService.extractUsername(token);
// if (username == null || !jwtService.isTokenValid(token)) {
// throw new RuntimeException("Invalid token");
// }
// } catch (Exception e) {
// exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
// return exchange.getResponse().setComplete();
// }

// return chain.filter(exchange);
// }
// }