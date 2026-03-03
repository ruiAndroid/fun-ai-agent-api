package com.fun.agent.api.repository;

import com.fun.agent.api.model.config.AgentConfigPayload;
import com.fun.agent.api.model.config.SkillConfigPayload;
import com.fun.agent.api.model.config.WorkflowConfigPayload;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class AgentConfigJdbcStore {

    private final JdbcTemplate jdbcTemplate;

    public AgentConfigJdbcStore(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<AgentConfigPayload> findAll() {
        List<AgentRow> agentRows = jdbcTemplate.query(
                """
                SELECT agent_id, display_name, owner_name, status, description, default_workflow_id
                FROM agent_cfg.agents
                ORDER BY agent_id
                """,
                (rs, rowNum) -> new AgentRow(
                        rs.getString("agent_id"),
                        rs.getString("display_name"),
                        rs.getString("owner_name"),
                        rs.getString("status"),
                        rs.getString("description"),
                        rs.getString("default_workflow_id")));

        List<WorkflowRow> workflowRows = jdbcTemplate.query(
                """
                SELECT agent_id, workflow_id, name, description, model_profile
                FROM agent_cfg.workflows
                ORDER BY agent_id, sort_order, workflow_id
                """,
                (rs, rowNum) -> new WorkflowRow(
                        rs.getString("agent_id"),
                        rs.getString("workflow_id"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getString("model_profile")));
        Map<String, List<WorkflowConfigPayload>> workflowsByAgent = new LinkedHashMap<>();
        for (WorkflowRow row : workflowRows) {
            workflowsByAgent.computeIfAbsent(row.agentId, ignored -> new ArrayList<>())
                    .add(new WorkflowConfigPayload(row.workflowId, row.name, row.description, row.modelProfile));
        }

        List<SkillRow> skillRows = jdbcTemplate.query(
                """
                SELECT agent_id, skill_id, name, prompt_template
                FROM agent_cfg.skills
                ORDER BY agent_id, sort_order, skill_id
                """,
                (rs, rowNum) -> new SkillRow(
                        rs.getString("agent_id"),
                        rs.getString("skill_id"),
                        rs.getString("name"),
                        rs.getString("prompt_template")));
        Map<String, List<SkillConfigPayload>> skillsByAgent = new LinkedHashMap<>();
        for (SkillRow row : skillRows) {
            skillsByAgent.computeIfAbsent(row.agentId, ignored -> new ArrayList<>())
                    .add(new SkillConfigPayload(row.skillId, row.name, row.promptTemplate));
        }

        List<AgentConfigPayload> result = new ArrayList<>(agentRows.size());
        for (AgentRow row : agentRows) {
            result.add(new AgentConfigPayload(
                    row.agentId,
                    row.displayName,
                    row.ownerName,
                    row.status,
                    row.description,
                    row.defaultWorkflowId,
                    workflowsByAgent.getOrDefault(row.agentId, List.of()),
                    skillsByAgent.getOrDefault(row.agentId, List.of())));
        }
        return result;
    }

    @Transactional
    public void replaceAll(List<AgentConfigPayload> agents) {
        jdbcTemplate.update("DELETE FROM agent_cfg.skills");
        jdbcTemplate.update("DELETE FROM agent_cfg.workflows");
        jdbcTemplate.update("DELETE FROM agent_cfg.agents");

        for (AgentConfigPayload agent : agents) {
            jdbcTemplate.update(
                    """
                    INSERT INTO agent_cfg.agents
                    (agent_id, display_name, owner_name, status, description, default_workflow_id, updated_at)
                    VALUES (?, ?, ?, ?, ?, ?, NOW())
                    """,
                    agent.id(),
                    agent.name(),
                    agent.owner(),
                    agent.status(),
                    agent.description(),
                    agent.defaultWorkflowId());

            int workflowSort = 1;
            for (WorkflowConfigPayload workflow : agent.workflows()) {
                jdbcTemplate.update(
                        """
                        INSERT INTO agent_cfg.workflows
                        (agent_id, workflow_id, name, description, model_profile, sort_order, updated_at)
                        VALUES (?, ?, ?, ?, ?, ?, NOW())
                        """,
                        agent.id(),
                        workflow.id(),
                        workflow.name(),
                        workflow.description(),
                        workflow.modelProfile(),
                        workflowSort++);
            }

            int skillSort = 1;
            for (SkillConfigPayload skill : agent.skills()) {
                jdbcTemplate.update(
                        """
                        INSERT INTO agent_cfg.skills
                        (agent_id, skill_id, name, prompt_template, sort_order, updated_at)
                        VALUES (?, ?, ?, ?, ?, NOW())
                        """,
                        agent.id(),
                        skill.id(),
                        skill.name(),
                        skill.promptTemplate(),
                        skillSort++);
            }
        }
    }

    public boolean updateSkillPrompt(String agentId, String skillId, String skillName, String promptTemplate) {
        int updated = jdbcTemplate.update(
                """
                UPDATE agent_cfg.skills
                SET name = ?, prompt_template = ?, updated_at = NOW()
                WHERE agent_id = ? AND skill_id = ?
                """,
                skillName,
                promptTemplate,
                agentId,
                skillId);
        return updated > 0;
    }

    private static final class AgentRow {
        private final String agentId;
        private final String displayName;
        private final String ownerName;
        private final String status;
        private final String description;
        private final String defaultWorkflowId;

        private AgentRow(
                String agentId,
                String displayName,
                String ownerName,
                String status,
                String description,
                String defaultWorkflowId) {
            this.agentId = agentId;
            this.displayName = displayName;
            this.ownerName = ownerName;
            this.status = status;
            this.description = description;
            this.defaultWorkflowId = defaultWorkflowId;
        }
    }

    private static final class WorkflowRow {
        private final String agentId;
        private final String workflowId;
        private final String name;
        private final String description;
        private final String modelProfile;

        private WorkflowRow(
                String agentId,
                String workflowId,
                String name,
                String description,
                String modelProfile) {
            this.agentId = agentId;
            this.workflowId = workflowId;
            this.name = name;
            this.description = description;
            this.modelProfile = modelProfile;
        }
    }

    private static final class SkillRow {
        private final String agentId;
        private final String skillId;
        private final String name;
        private final String promptTemplate;

        private SkillRow(String agentId, String skillId, String name, String promptTemplate) {
            this.agentId = agentId;
            this.skillId = skillId;
            this.name = name;
            this.promptTemplate = promptTemplate;
        }
    }
}
