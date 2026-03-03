package com.fun.agent.api.client;

import com.fun.agent.api.config.GatewayProperties;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

@Component
public class ModelGatewayClient {

    private static final ParameterizedTypeReference<Map<String, Object>> MAP_TYPE =
            new ParameterizedTypeReference<>() {
            };

    private final WebClient webClient;
    private final Duration timeout;
    private final GatewayProperties properties;
    private final AtomicReference<Map<String, Object>> modelsCache = new AtomicReference<>();
    private final AtomicLong cacheExpireAtEpochMs = new AtomicLong(0L);

    public ModelGatewayClient(
            @Qualifier("gatewayWebClient") WebClient gatewayWebClient,
            @Qualifier("gatewayTimeout") Duration gatewayTimeout,
            GatewayProperties properties) {
        this.webClient = gatewayWebClient;
        this.timeout = gatewayTimeout;
        this.properties = properties;
    }

    public Mono<Map<String, Object>> listModels() {
        long now = System.currentTimeMillis();
        Map<String, Object> cached = modelsCache.get();
        if (cached != null && now < cacheExpireAtEpochMs.get()) {
            return Mono.just(cached);
        }

        String token = properties.token() == null ? "" : properties.token().trim();
        if (token.isEmpty()) {
            return Mono.error(new IllegalStateException("GATEWAY_TOKEN is not configured."));
        }

        return webClient.get()
                .uri("models")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .retrieve()
                .onStatus(HttpStatusCode::isError, this::mapError)
                .bodyToMono(MAP_TYPE)
                .timeout(timeout)
                .doOnNext(this::refreshCache);
    }

    private void refreshCache(Map<String, Object> payload) {
        int cacheSeconds = Math.max(0, properties.modelsCacheSeconds());
        modelsCache.set(payload);
        cacheExpireAtEpochMs.set(System.currentTimeMillis() + (cacheSeconds * 1000L));
    }

    private Mono<? extends Throwable> mapError(org.springframework.web.reactive.function.client.ClientResponse response) {
        return response.bodyToMono(String.class).defaultIfEmpty("gateway request failed").map(message ->
                new ResponseStatusException(response.statusCode(), message));
    }
}
