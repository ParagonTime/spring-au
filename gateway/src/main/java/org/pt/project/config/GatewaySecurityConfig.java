package org.pt.project.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.firewall.ServerWebExchangeFirewall;
import org.springframework.security.web.server.firewall.StrictServerWebExchangeFirewall;

@Configuration
@EnableWebFluxSecurity
public class GatewaySecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers(
                                "/registration",
                                "/registration/**",
                                "/api/auth/**",
                                "/actuator/prometheus",
                                "/actuator/metrics",
                                "/api/v1/metrics/**"
                        ).permitAll()
                        .anyExchange().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> {})
                );
        return http.build();
    }

    @Bean
    public ServerWebExchangeFirewall serverWebExchangeFirewall() {
        StrictServerWebExchangeFirewall firewall = new StrictServerWebExchangeFirewall();
        firewall.setAllowedHeaderNames(name -> true);
        firewall.setAllowedHeaderValues(value -> true);
        return firewall;
    }
}