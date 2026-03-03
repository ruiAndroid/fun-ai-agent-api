package com.fun.agent.api.model.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.Map;

public record SkillConfigPayload(
        @NotBlank @Size(max = 128) String id,
        @NotBlank @Size(max = 256) String name,
        @NotBlank @Size(max = 12000) String promptTemplate,
        Map<String, String> promptVariants
) {
}
