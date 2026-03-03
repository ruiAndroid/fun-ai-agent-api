package com.fun.agent.api.controller;

import com.fun.agent.api.client.ModelGatewayClient;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping({"/api/v1/models", "/v1/models"})
public class ModelController {

    private final ModelGatewayClient modelGatewayClient;

    public ModelController(ModelGatewayClient modelGatewayClient) {
        this.modelGatewayClient = modelGatewayClient;
    }

    @GetMapping
    public Mono<Map<String, Object>> listModels() {
        return modelGatewayClient.listModels();
    }
}
