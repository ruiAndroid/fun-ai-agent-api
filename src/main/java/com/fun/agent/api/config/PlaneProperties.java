package com.fun.agent.api.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "plane")
public record PlaneProperties(String baseUrl, int timeoutSeconds) {
}

