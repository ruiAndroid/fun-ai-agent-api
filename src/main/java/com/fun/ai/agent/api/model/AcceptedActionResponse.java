package com.fun.ai.agent.api.model;

import java.time.Instant;
import java.util.UUID;

public record AcceptedActionResponse(
        UUID taskId,
        Instant acceptedAt
) {
}
