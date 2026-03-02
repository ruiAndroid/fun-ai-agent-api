package com.fun.agent.api.controller;

import com.fun.agent.api.client.PlaneClient;
import com.fun.agent.api.model.CreateTaskRequest;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/tasks")
public class TaskGatewayController {

    private final PlaneClient planeClient;

    public TaskGatewayController(PlaneClient planeClient) {
        this.planeClient = planeClient;
    }

    @PostMapping
    public Mono<ResponseEntity<Map<String, Object>>> createTask(@Valid @RequestBody CreateTaskRequest request) {
        return planeClient
                .createTask(request.toPlanePayload())
                .map(body -> ResponseEntity.accepted().body(body));
    }

    @GetMapping("/{taskId}")
    public Mono<Map<String, Object>> getTask(@PathVariable String taskId) {
        return planeClient.getTask(taskId);
    }

    @PostMapping("/{taskId}/cancel")
    public Mono<Map<String, Object>> cancelTask(@PathVariable String taskId) {
        return planeClient.cancelTask(taskId);
    }

    @GetMapping(value = "/{taskId}/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamTaskEvents(@PathVariable String taskId) {
        return planeClient.streamTaskEvents(taskId);
    }
}
