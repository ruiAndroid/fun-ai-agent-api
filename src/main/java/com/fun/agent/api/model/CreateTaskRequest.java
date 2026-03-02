package com.fun.agent.api.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.LinkedHashMap;
import java.util.Map;

public record CreateTaskRequest(
        @NotBlank @Size(max = 128) String tenantId,
        @NotBlank @Size(max = 128) String agentId,
        @NotBlank @Size(max = 6000) String prompt,
        @Size(max = 128) String idempotencyKey
) {

    public Map<String, Object> toPlanePayload() {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("tenant_id", tenantId);
        payload.put("agent_id", agentId);
        payload.put("prompt", prompt);
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            payload.put("idempotency_key", idempotencyKey);
        }
        return payload;
    }
}

