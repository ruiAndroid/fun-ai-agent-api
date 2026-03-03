package com.fun.agent.api.model.config;

import jakarta.validation.Valid;
import java.util.List;

public record ReplaceAgentConfigsRequest(
        List<@Valid AgentConfigPayload> agents
) {
    public List<AgentConfigPayload> safeAgents() {
        return agents == null ? List.of() : agents;
    }
}
