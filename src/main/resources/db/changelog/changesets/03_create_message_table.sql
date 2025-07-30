DROP TABLE IF EXISTS messages CASCADE;

CREATE TABLE messages(
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    sender_id TEXT NOT NULL REFERENCES users(google_sub) ON DELETE CASCADE,
    chat_id UUID NOT NULL REFERENCES chats(id) ON DELETE CASCADE,
    content_type VARCHAR(15) NOT NULL CHECK(content_type IN ('image/png','image/jpeg','video/mp4','text/plain')),
    content TEXT NOT NULL,
    media_content TEXT,
    CHECK (
            (content_type = 'text/plain' AND media_content IS NULL) OR
            (content_type IN ('image/png','image/jpeg', 'video/mp4') AND media_content IS NOT NULL)
    )

)