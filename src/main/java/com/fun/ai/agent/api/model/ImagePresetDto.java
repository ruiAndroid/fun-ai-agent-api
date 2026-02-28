package com.fun.ai.agent.api.model;

public record ImagePresetDto(
        String id,
        String name,
        String image,
        InstanceRuntime runtime,
        String description,
        boolean recommended
) {
}
