ALTER TABLE messages
    ADD COLUMN sources_json JSONB NOT NULL DEFAULT '[]'::jsonb;
