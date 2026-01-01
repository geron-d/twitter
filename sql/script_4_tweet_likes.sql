-- Create tweet_likes table
CREATE TABLE IF NOT EXISTS tweet_likes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tweet_id UUID NOT NULL,
    user_id UUID NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT tweet_likes_tweet_fk FOREIGN KEY (tweet_id) REFERENCES tweets(id),
    CONSTRAINT tweet_likes_user_fk FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT uk_tweet_likes_tweet_user UNIQUE (tweet_id, user_id)
);

