-- Create tweets table
CREATE TABLE IF NOT EXISTS tweets (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    content VARCHAR(280) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN NOT NULL DEFAULT false,
    deleted_at TIMESTAMP NULL,

    CONSTRAINT chk_content_length CHECK (LENGTH(TRIM(content)) > 0),
    CONSTRAINT chk_content_max_length CHECK (LENGTH(content) <= 280)
);

-- Create indexes for performance optimization
-- Index for user queries (most common use case)
CREATE INDEX IF NOT EXISTS idx_tweets_user_id_created_at 
ON tweets(user_id, created_at DESC);

-- Index for chronological feed queries
CREATE INDEX IF NOT EXISTS idx_tweets_created_at_desc 
ON tweets(created_at DESC);

-- Index for soft delete filtering (optimizes queries filtering by is_deleted = false)
CREATE INDEX IF NOT EXISTS idx_tweets_is_deleted 
ON tweets(is_deleted);

-- Create function to automatically update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_tweets_updated_at 
    BEFORE UPDATE ON tweets 
    FOR EACH ROW 
    EXECUTE FUNCTION update_updated_at_column();
