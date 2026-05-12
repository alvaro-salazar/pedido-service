package com.denkitronik.pedidoservice.infrastructure.clients;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
public class JwtFeignInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate template) {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
            String token = jwtAuth.getToken().getTokenValue();
            template.header("Authorization", "Bearer " + token);
        }
    }
}
