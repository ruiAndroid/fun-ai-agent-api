package com.fun.agent.api.controller;

import com.fun.agent.api.model.config.AgentConfigsResponse;
import com.fun.agent.api.model.config.ReplaceAgentConfigsRequest;
import com.fun.agent.api.model.config.SkillConfigPayload;
import com.fun.agent.api.model.config.UpdateSkillPromptRequest;
import com.fun.agent.api.service.AgentConfigService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping({"/api/v1/config", "/v1/config"})
public class AgentConfigController {

    private final AgentConfigService agentConfigService;

    public AgentConfigController(AgentConfigService agentConfigService) {
        this.agentConfigService = agentConfigService;
    }

    @GetMapping("/agents")
    public Mono<AgentConfigsResponse> listAgents() {
        return agentConfigService
                .listAgentConfigs()
                .map(AgentConfigsResponse::new);
    }

    @PutMapping("/agents")
    public Mono<AgentConfigsResponse> replaceAgents(@Valid @RequestBody ReplaceAgentConfigsRequest request) {
        return agentConfigService
                .replaceAll(request.safeAgents())
                .map(AgentConfigsResponse::new);
    }

    @PutMapping("/agents/{agentId}/skills/{skillId}")
    public Mono<SkillConfigPayload> updateSkillPrompt(
            @PathVariable String agentId,
            @PathVariable String skillId,
            @Valid @RequestBody UpdateSkillPromptRequest request) {
        return agentConfigService.updateSkillPrompt(
                agentId,
                skillId,
                request.name(),
                request.promptTemplate());
    }
}
