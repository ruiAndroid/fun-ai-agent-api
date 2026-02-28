package com.fun.ai.agent.api.model;

import java.time.Instant;
import java.util.UUID;

public record ClawInstanceDto(
        UUID id,
        String name,
        UUID hostId,
        InstanceRuntime runtime,
        InstanceStatus status,
        InstanceDesiredState desiredState,
        Instant createdAt,
        Instant updatedAt
) {
}
