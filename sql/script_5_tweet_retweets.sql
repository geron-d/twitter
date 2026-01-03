-- Create tweet_retweets table
CREATE TABLE IF NOT EXISTS tweet_retweets (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tweet_id UUID NOT NULL,
    user_id UUID NOT NULL,
    comment VARCHAR(280) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT tweet_retweets_tweet_fk FOREIGN KEY (tweet_id) REFERENCES tweets(id),
    CONSTRAINT tweet_retweets_user_fk FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT uk_tweet_retweets_tweet_user UNIQUE (tweet_id, user_id)
);

