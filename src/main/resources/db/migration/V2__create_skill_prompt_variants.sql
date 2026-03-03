CREATE TABLE IF NOT EXISTS agent_cfg.skill_prompt_variants (
    agent_id VARCHAR(128) NOT NULL,
    skill_id VARCHAR(128) NOT NULL,
    variant_key VARCHAR(128) NOT NULL,
    prompt_template TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT pk_skill_prompt_variants PRIMARY KEY (agent_id, skill_id, variant_key),
    CONSTRAINT fk_skill_prompt_variants_skill FOREIGN KEY (agent_id, skill_id)
        REFERENCES agent_cfg.skills(agent_id, skill_id)
        ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_skill_prompt_variants_agent_skill
    ON agent_cfg.skill_prompt_variants(agent_id, skill_id, variant_key);
