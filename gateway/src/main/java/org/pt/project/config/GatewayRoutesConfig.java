package org.pt.project.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayRoutesConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("auth-public", r -> r
                        .path("/registration", "/registration/**", "/api/auth/**", "/users", "/users/**")
                        .uri("http://auth-service:8081"))
                .route("task-api", r -> r
                        .path("/tasks", "/tasks/**")
                        .uri("http://app-service:8080"))
                .route("search-api", r -> r
                        .path("/search", "/search/**")
                        .uri("http://search-service:8080"))
                .route("observability-service", r -> r
                        .path("/api/v1/metrics/**")
                        .uri("http://observability-service.observability.svc.cluster.local:8080"))
                .build();
    }
}