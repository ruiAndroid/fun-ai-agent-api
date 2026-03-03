package com.fun.agent.api.model.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateSkillPromptRequest(
        @NotBlank @Size(max = 256) String name,
        @NotBlank @Size(max = 12000) String promptTemplate
) {
}
