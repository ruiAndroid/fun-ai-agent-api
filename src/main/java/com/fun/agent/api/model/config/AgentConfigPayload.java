package com.fun.agent.api.model.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

public record AgentConfigPayload(
        @NotBlank @Size(max = 128) String id,
        @NotBlank @Size(max = 256) String name,
        @Size(max = 128) String owner,
        @Size(max = 16) String status,
        @Size(max = 2000) String description,
        @Size(max = 128) String defaultWorkflowId,
        List<@Valid WorkflowConfigPayload> workflows,
        List<@Valid SkillConfigPayload> skills
) {
}
