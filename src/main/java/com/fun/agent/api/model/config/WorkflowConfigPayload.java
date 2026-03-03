package com.fun.agent.api.model.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record WorkflowConfigPayload(
        @NotBlank @Size(max = 128) String id,
        @NotBlank @Size(max = 256) String name,
        @Size(max = 2000) String description,
        @Size(max = 128) String modelProfile
) {
}
