CREATE TABLE IF NOT EXISTS permission (
      id         SERIAL PRIMARY KEY
    , permission TEXT UNIQUE NOT NULL
);
--;;
INSERT INTO permission (permission) VALUES ('basic');
--;;
CREATE TABLE IF NOT EXISTS registered_user (
     id            UUID PRIMARY KEY DEFAULT UUID_GENERATE_V4()
   , created_on    TIMESTAMP DEFAULT CURRENT_TIMESTAMP
   , email         CITEXT             NOT NULL UNIQUE
   , username      CITEXT             NOT NULL UNIQUE
   , password      TEXT               NOT NULL
   , refresh_token TEXT
   , indexable     BOOLEAN            NOT NULL DEFAULT TRUE
);
--;;
CREATE TABLE IF NOT EXISTS connected_users (
     user_sender_id     UUID REFERENCES registered_user(id)    ON DELETE CASCADE
   , user_receiver_id   UUID REFERENCES registered_user(id)    ON DELETE CASCADE
   , status             SMALLINT           NOT NULL DEFAULT 0
   , created_on         TIMESTAMP DEFAULT CURRENT_TIMESTAMP
   , PRIMARY KEY (user_sender_id, user_receiver_id)
);
--;;
CREATE TABLE IF NOT EXISTS user_permission (
      id         SERIAL  PRIMARY KEY
    , user_id    UUID    REFERENCES registered_user (id)    ON DELETE CASCADE
    , permission TEXT    NOT NULL DEFAULT 'basic'
);
--;;
CREATE TABLE IF NOT EXISTS password_reset_key (
    id            SERIAL    PRIMARY KEY NOT NULL
  , reset_key     TEXT                  NOT NULL UNIQUE
  , already_used  BOOLEAN               NOT NULL DEFAULT FALSE
  , user_id       UUID      REFERENCES registered_user (id) ON DELETE CASCADE
  , valid_until   TIMESTAMP WITH TIME ZONE DEFAULT CLOCK_TIMESTAMP() + INTERVAL '24 hours'
);
