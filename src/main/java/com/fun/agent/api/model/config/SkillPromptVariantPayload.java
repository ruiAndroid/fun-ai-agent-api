package com.fun.agent.api.model.config;

public record SkillPromptVariantPayload(
        String agentId,
        String skillId,
        String variantKey,
        String promptTemplate
) {
}
