package com.fun.agent.api.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "gateway")
public record GatewayProperties(
        String baseUrl,
        String token,
        int timeoutSeconds,
        int modelsCacheSeconds) {
}
