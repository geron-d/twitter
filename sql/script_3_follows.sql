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





