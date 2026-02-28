package com.fun.ai.agent.api.controller;

import com.fun.ai.agent.api.model.AcceptedActionResponse;
import com.fun.ai.agent.api.model.CreateInstanceRequest;
import com.fun.ai.agent.api.model.InstanceActionRequest;
import com.fun.ai.agent.api.model.ListResponse;
import com.fun.ai.agent.api.model.ClawInstanceDto;
import com.fun.ai.agent.api.service.ControlService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/v1/instances")
public class InstanceController {

    private final ControlService controlService;

    public InstanceController(ControlService controlService) {
        this.controlService = controlService;
    }

    @GetMapping
    public ListResponse<ClawInstanceDto> listInstances() {
        return new ListResponse<>(controlService.listInstances());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ClawInstanceDto createInstance(@Valid @RequestBody CreateInstanceRequest request) {
        return controlService.createInstance(request);
    }

    @PostMapping("/{instanceId}/actions")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public AcceptedActionResponse submitAction(@PathVariable UUID instanceId,
                                               @Valid @RequestBody InstanceActionRequest request) {
        return controlService.submitInstanceAction(instanceId, request);
    }
}
