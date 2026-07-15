CREATE TABLE click_records (
    id BIGSERIAL PRIMARY KEY,
    short_code VARCHAR(12) NOT NULL,
    original_url VARCHAR(2048) NOT NULL,
    occurred_at TIMESTAMP NOT NULL,
    user_agent VARCHAR(512),
    ip_hash VARCHAR(16),
    referer VARCHAR(1024)
);

CREATE INDEX idx_click_records_short_code ON click_records (short_code);
