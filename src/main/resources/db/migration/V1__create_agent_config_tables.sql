CREATE SCHEMA IF NOT EXISTS agent_cfg;

CREATE TABLE IF NOT EXISTS agent_cfg.agents (
    agent_id VARCHAR(128) PRIMARY KEY,
    display_name VARCHAR(256) NOT NULL,
    owner_name VARCHAR(128) NOT NULL DEFAULT '',
    status VARCHAR(16) NOT NULL DEFAULT 'ONLINE',
    description TEXT NOT NULL DEFAULT '',
    default_workflow_id VARCHAR(128) NOT NULL DEFAULT '',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT ck_agents_status CHECK (status IN ('ONLINE', 'DEGRADED', 'OFFLINE'))
);

CREATE TABLE IF NOT EXISTS agent_cfg.workflows (
    agent_id VARCHAR(128) NOT NULL,
    workflow_id VARCHAR(128) NOT NULL,
    name VARCHAR(256) NOT NULL,
    description TEXT NOT NULL DEFAULT '',
    model_profile VARCHAR(128) NOT NULL DEFAULT '',
    sort_order INT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT pk_workflows PRIMARY KEY (agent_id, workflow_id),
    CONSTRAINT fk_workflows_agent FOREIGN KEY (agent_id)
        REFERENCES agent_cfg.agents(agent_id)
        ON DELETE CASCADE,
    CONSTRAINT ck_workflows_sort_order CHECK (sort_order > 0)
);

CREATE TABLE IF NOT EXISTS agent_cfg.skills (
    agent_id VARCHAR(128) NOT NULL,
    skill_id VARCHAR(128) NOT NULL,
    name VARCHAR(256) NOT NULL,
    prompt_template TEXT NOT NULL,
    sort_order INT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT pk_skills PRIMARY KEY (agent_id, skill_id),
    CONSTRAINT fk_skills_agent FOREIGN KEY (agent_id)
        REFERENCES agent_cfg.agents(agent_id)
        ON DELETE CASCADE,
    CONSTRAINT ck_skills_sort_order CHECK (sort_order > 0)
);

CREATE INDEX IF NOT EXISTS idx_workflows_agent_sort
    ON agent_cfg.workflows(agent_id, sort_order, workflow_id);

CREATE INDEX IF NOT EXISTS idx_skills_agent_sort
    ON agent_cfg.skills(agent_id, sort_order, skill_id);
