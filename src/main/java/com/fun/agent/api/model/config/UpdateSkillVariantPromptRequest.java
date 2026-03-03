package com.fun.agent.api.model.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateSkillVariantPromptRequest(
        @NotBlank @Size(max = 12000) String promptTemplate
) {
}
