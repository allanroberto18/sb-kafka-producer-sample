ALTER TABLE outbox_events
    ADD COLUMN attempt_count INTEGER NOT NULL DEFAULT 0;

CREATE TABLE invoice_emails
(
    id              UUID PRIMARY KEY,
    user_id         BIGINT                   NOT NULL REFERENCES users (id),
    order_id        BIGINT                   NOT NULL REFERENCES orders (id),
    recipient_email VARCHAR(255)             NOT NULL,
    subject         VARCHAR(255)             NOT NULL,
    body            TEXT                     NOT NULL,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL,
    sent_at         TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_outbox_status_attempt_created_at ON outbox_events (status, attempt_count, created_at);
CREATE INDEX idx_invoice_emails_user_id ON invoice_emails (user_id);
CREATE INDEX idx_invoice_emails_order_id ON invoice_emails (order_id);
