CREATE TABLE rule_audit (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    rule_key VARCHAR(255) NOT NULL,
    execution_time BIGINT NOT NULL,
    success BOOLEAN NOT NULL,
    error_message TEXT,
    created_at TIMESTAMP NOT NULL,
    
    -- Indexes for common queries
    INDEX idx_rule_key (rule_key),
    INDEX idx_created_at (created_at),
    INDEX idx_success (success)
);

-- Comments for documentation
COMMENT ON TABLE rule_audit IS 'Stores execution metrics and audit trail for rule engine';
COMMENT ON COLUMN rule_audit.id IS 'Unique identifier for the audit entry';
COMMENT ON COLUMN rule_audit.rule_key IS 'Identifier of the rule that was executed';
COMMENT ON COLUMN rule_audit.execution_time IS 'Time taken to execute the rule in milliseconds';
COMMENT ON COLUMN rule_audit.success IS 'Whether the rule execution was successful';
COMMENT ON COLUMN rule_audit.error_message IS 'Error message if the rule execution failed';
COMMENT ON COLUMN rule_audit.created_at IS 'Timestamp when the audit entry was created';