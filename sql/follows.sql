-- Create follows table
CREATE TABLE IF NOT EXISTS follows (
    id UUID PRIMARY KEY,
    follower_id UUID NOT NULL,
    following_id UUID NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT follows_follower_fk FOREIGN KEY (follower_id) REFERENCES users(id),
    CONSTRAINT follows_following_fk FOREIGN KEY (following_id) REFERENCES users(id),
    CONSTRAINT follows_unique_follower_following UNIQUE (follower_id, following_id),
    CONSTRAINT follows_check_no_self_follow CHECK (follower_id != following_id)
);

-- Create indexes for performance optimization
-- Index for queries by follower (who is following)
CREATE INDEX IF NOT EXISTS idx_follows_follower_id 
ON follows(follower_id);

-- Index for queries by following (who are the followers)
CREATE INDEX IF NOT EXISTS idx_follows_following_id 
ON follows(following_id);

-- Index for sorting by creation date
CREATE INDEX IF NOT EXISTS idx_follows_created_at 
ON follows(created_at);




