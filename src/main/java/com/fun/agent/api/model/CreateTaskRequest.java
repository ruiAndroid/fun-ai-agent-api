package com.fun.agent.api.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.LinkedHashMap;
import java.util.Map;

public record CreateTaskRequest(
        @JsonAlias("tenant_id")
        @NotBlank @Size(max = 128) String tenantId,
        @JsonAlias("agent_id")
        @NotBlank @Size(max = 128) String agentId,
        @JsonAlias("workflow_id")
        @Size(max = 128) String workflowId,
        @JsonAlias("skill_id")
        @Size(max = 128) String skillId,
        @JsonAlias("skill_prompt_override")
        @Size(max = 12000) String skillPromptOverride,
        @JsonAlias("skill_prompt_overrides")
        Map<String, String> skillPromptOverrides,
        @JsonAlias("input_payload")
        Map<String, Object> inputPayload,
        @NotBlank @Size(max = 6000) String prompt,
        @JsonAlias("idempotency_key")
        @Size(max = 128) String idempotencyKey
) {

    public Map<String, Object> toPlanePayload() {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("tenant_id", tenantId);
        payload.put("agent_id", agentId);
        payload.put("prompt", prompt);
        if (workflowId != null && !workflowId.isBlank()) {
            payload.put("workflow_id", workflowId);
        }
        if (skillId != null && !skillId.isBlank()) {
            payload.put("skill_id", skillId);
        }
        if (skillPromptOverride != null && !skillPromptOverride.isBlank()) {
            payload.put("skill_prompt_override", skillPromptOverride);
        }
        Map<String, String> sanitizedOverrides = sanitizeSkillPromptOverrides(skillPromptOverrides);
        if (!sanitizedOverrides.isEmpty()) {
            payload.put("skill_prompt_overrides", sanitizedOverrides);
        }
        if (inputPayload != null && !inputPayload.isEmpty()) {
            payload.put("input_payload", inputPayload);
        }
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            payload.put("idempotency_key", idempotencyKey);
        }
        return payload;
    }

    private static Map<String, String> sanitizeSkillPromptOverrides(Map<String, String> raw) {
        Map<String, String> sanitized = new LinkedHashMap<>();
        if (raw == null || raw.isEmpty()) {
            return sanitized;
        }
        for (Map.Entry<String, String> entry : raw.entrySet()) {
            if (entry.getKey() == null || entry.getValue() == null) {
                continue;
            }
            String key = entry.getKey().trim();
            String value = entry.getValue().trim();
            if (key.isEmpty() || value.isEmpty()) {
                continue;
            }
            sanitized.put(key, value);
        }
        return sanitized;
    }
}
