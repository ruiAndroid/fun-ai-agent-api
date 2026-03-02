package com.fun.agent.api.client;

import java.time.Duration;
import java.util.Map;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class PlaneClient {

    private static final ParameterizedTypeReference<Map<String, Object>> MAP_TYPE =
            new ParameterizedTypeReference<>() {
            };

    private final WebClient webClient;
    private final Duration timeout;

    public PlaneClient(WebClient planeWebClient, Duration planeTimeout) {
        this.webClient = planeWebClient;
        this.timeout = planeTimeout;
    }

    public Mono<Map<String, Object>> createTask(Map<String, Object> payload) {
        return webClient.post()
                .uri("/v1/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payload)
                .retrieve()
                .onStatus(HttpStatusCode::isError, this::mapError)
                .bodyToMono(MAP_TYPE)
                .timeout(timeout);
    }

    public Mono<Map<String, Object>> getTask(String taskId) {
        return webClient.get()
                .uri("/v1/tasks/{taskId}", taskId)
                .retrieve()
                .onStatus(HttpStatusCode::isError, this::mapError)
                .bodyToMono(MAP_TYPE)
                .timeout(timeout);
    }

    public Mono<Map<String, Object>> cancelTask(String taskId) {
        return webClient.post()
                .uri("/v1/tasks/{taskId}/cancel", taskId)
                .retrieve()
                .onStatus(HttpStatusCode::isError, this::mapError)
                .bodyToMono(MAP_TYPE)
                .timeout(timeout);
    }

    public Flux<String> streamTaskEvents(String taskId) {
        return webClient.get()
                .uri("/v1/tasks/{taskId}/events", taskId)
                .accept(MediaType.TEXT_EVENT_STREAM)
                .retrieve()
                .onStatus(HttpStatusCode::isError, this::mapError)
                .bodyToFlux(String.class);
    }

    private Mono<? extends Throwable> mapError(org.springframework.web.reactive.function.client.ClientResponse response) {
        return response.bodyToMono(String.class).defaultIfEmpty("plane request failed").map(message ->
                new ResponseStatusException(response.statusCode(), message));
    }
}

