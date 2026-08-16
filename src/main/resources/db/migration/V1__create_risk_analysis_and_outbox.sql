CREATE TABLE risk_analyses (
    id UUID PRIMARY KEY,
    idempotency_key VARCHAR(100) NOT NULL UNIQUE,
    request_fingerprint VARCHAR(64) NOT NULL,
    transaction_id VARCHAR(100) NOT NULL,
    customer_id VARCHAR(100) NOT NULL,
    amount NUMERIC(19, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    origin_country VARCHAR(2) NOT NULL,
    card_country VARCHAR(2) NOT NULL,
    merchant_category VARCHAR(4) NOT NULL,
    occurred_at TIMESTAMP WITH TIME ZONE NOT NULL,
    score INTEGER NOT NULL CHECK (score BETWEEN 0 AND 100),
    decision VARCHAR(20) NOT NULL,
    analyzed_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE risk_analysis_factors (
    analysis_id UUID NOT NULL REFERENCES risk_analyses(id) ON DELETE CASCADE,
    factor_order INTEGER NOT NULL,
    factor_code VARCHAR(50) NOT NULL,
    points INTEGER NOT NULL CHECK (points > 0),
    explanation VARCHAR(255) NOT NULL,
    PRIMARY KEY (analysis_id, factor_order)
);

CREATE TABLE outbox_events (
    id UUID PRIMARY KEY,
    aggregate_id UUID NOT NULL REFERENCES risk_analyses(id),
    event_type VARCHAR(100) NOT NULL,
    payload TEXT NOT NULL,
    status VARCHAR(20) NOT NULL,
    attempts INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    published_at TIMESTAMP WITH TIME ZONE,
    last_error VARCHAR(500),
    version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_risk_analyses_customer_occurred
    ON risk_analyses(customer_id, occurred_at);

CREATE INDEX idx_outbox_status_created
    ON outbox_events(status, created_at);
