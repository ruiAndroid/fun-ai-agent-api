package com.fun.agent.api.service;

import com.fun.agent.api.model.config.AgentConfigPayload;
import com.fun.agent.api.model.config.SkillConfigPayload;
import com.fun.agent.api.model.config.WorkflowConfigPayload;
import com.fun.agent.api.repository.AgentConfigJdbcStore;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import org.springframework.http.HttpStatus;

@Service
public class AgentConfigService {

    private static final Set<String> ALLOWED_STATUS = Set.of("ONLINE", "DEGRADED", "OFFLINE");

    private final AgentConfigJdbcStore store;

    public AgentConfigService(AgentConfigJdbcStore store) {
        this.store = store;
    }

    public Mono<List<AgentConfigPayload>> listAgentConfigs() {
        return Mono.fromCallable(store::findAll)
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<List<AgentConfigPayload>> replaceAll(List<AgentConfigPayload> rawAgents) {
        return Mono.fromCallable(() -> {
                    List<AgentConfigPayload> sanitized = sanitizeAgents(rawAgents);
                    store.replaceAll(sanitized);
                    return store.findAll();
                })
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<SkillConfigPayload> updateSkillPrompt(String agentId, String skillId, String name, String promptTemplate) {
        return Mono.fromCallable(() -> {
                    String normalizedAgentId = normalizeRequired(agentId);
                    String normalizedSkillId = normalizeRequired(skillId);
                    String normalizedName = normalizeOptional(name, normalizedSkillId);
                    String normalizedPrompt = normalizeOptional(promptTemplate, "");
                    if (normalizedPrompt.isEmpty()) {
                        throw new IllegalArgumentException("promptTemplate must not be empty");
                    }

                    boolean updated = store.updateSkillPrompt(
                            normalizedAgentId,
                            normalizedSkillId,
                            normalizedName,
                            normalizedPrompt);
                    if (!updated) {
                        throw new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "skill not found for agent: " + normalizedAgentId + "/" + normalizedSkillId);
                    }
                    return new SkillConfigPayload(normalizedSkillId, normalizedName, normalizedPrompt);
                })
                .subscribeOn(Schedulers.boundedElastic());
    }

    private List<AgentConfigPayload> sanitizeAgents(List<AgentConfigPayload> rawAgents) {
        if (rawAgents == null || rawAgents.isEmpty()) {
            return List.of();
        }

        Map<String, AgentConfigPayload> deduped = new LinkedHashMap<>();
        for (AgentConfigPayload raw : rawAgents) {
            if (raw == null) {
                continue;
            }
            AgentConfigPayload sanitized = sanitizeAgent(raw);
            deduped.put(sanitized.id(), sanitized);
        }
        return new ArrayList<>(deduped.values());
    }

    private AgentConfigPayload sanitizeAgent(AgentConfigPayload raw) {
        String agentId = normalizeRequired(raw.id());
        String name = normalizeOptional(raw.name(), agentId);
        String owner = normalizeOptional(raw.owner(), "");
        String status = normalizeStatus(raw.status());
        String description = normalizeOptional(raw.description(), "");

        List<WorkflowConfigPayload> workflows = sanitizeWorkflows(raw.workflows());
        List<SkillConfigPayload> skills = sanitizeSkills(raw.skills());
        String defaultWorkflowId = normalizeDefaultWorkflowId(raw.defaultWorkflowId(), workflows);

        return new AgentConfigPayload(
                agentId,
                name,
                owner,
                status,
                description,
                defaultWorkflowId,
                workflows,
                skills);
    }

    private List<WorkflowConfigPayload> sanitizeWorkflows(List<WorkflowConfigPayload> rawWorkflows) {
        if (rawWorkflows == null || rawWorkflows.isEmpty()) {
            return List.of();
        }
        Map<String, WorkflowConfigPayload> deduped = new LinkedHashMap<>();
        for (WorkflowConfigPayload raw : rawWorkflows) {
            if (raw == null) {
                continue;
            }
            String id = normalizeRequired(raw.id());
            deduped.put(id, new WorkflowConfigPayload(
                    id,
                    normalizeOptional(raw.name(), id),
                    normalizeOptional(raw.description(), ""),
                    normalizeOptional(raw.modelProfile(), "")));
        }
        return new ArrayList<>(deduped.values());
    }

    private List<SkillConfigPayload> sanitizeSkills(List<SkillConfigPayload> rawSkills) {
        if (rawSkills == null || rawSkills.isEmpty()) {
            return List.of();
        }
        Map<String, SkillConfigPayload> deduped = new LinkedHashMap<>();
        for (SkillConfigPayload raw : rawSkills) {
            if (raw == null) {
                continue;
            }
            String id = normalizeRequired(raw.id());
            deduped.put(id, new SkillConfigPayload(
                    id,
                    normalizeOptional(raw.name(), id),
                    normalizeOptional(raw.promptTemplate(), "")));
        }
        return new ArrayList<>(deduped.values());
    }

    private String normalizeDefaultWorkflowId(String rawDefault, List<WorkflowConfigPayload> workflows) {
        if (workflows.isEmpty()) {
            return "";
        }
        String defaultId = normalizeOptional(rawDefault, "");
        if (defaultId.isEmpty()) {
            return workflows.get(0).id();
        }
        Set<String> workflowIds = new LinkedHashSet<>();
        for (WorkflowConfigPayload workflow : workflows) {
            workflowIds.add(workflow.id());
        }
        if (!workflowIds.contains(defaultId)) {
            return workflows.get(0).id();
        }
        return defaultId;
    }

    private String normalizeStatus(String status) {
        String normalized = normalizeOptional(status, "ONLINE").toUpperCase();
        return ALLOWED_STATUS.contains(normalized) ? normalized : "ONLINE";
    }

    private String normalizeRequired(String value) {
        String normalized = normalizeOptional(value, "");
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("id must not be empty");
        }
        return normalized;
    }

    private String normalizeOptional(String value, String fallback) {
        if (value == null) {
            return fallback;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? fallback : normalized;
    }
}
