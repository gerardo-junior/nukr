-- :name get-connection-by-user-ids
-- :command :query
-- :result :one
-- :doc Get status of connection
SELECT user_sender_id
     , user_receiver_id
     , status
FROM   connected_users
WHERE  (user_sender_id = :user-a-id AND user_receiver_id = :user-b-id)
OR     (user_sender_id = :user-b-id AND user_receiver_id = :user-a-id)

-- :name insert-connection!
-- :command :insert
-- :result :raw
-- :doc Inserts a single connection
INSERT INTO connected_users (
      user_sender_id
    , user_receiver_id
    , status)
VALUES (
      :user-sender-id
    , :user-receiver-id
    , :status);

-- :name delete-connection!
-- :command :execute
-- :result :affected
-- :doc Delete a single connection matching provided user id's
DELETE FROM connected_users
WHERE  (user_sender_id = :user-a-id AND user_receiver_id = :user-b-id)
OR     (user_sender_id = :user-b-id AND user_receiver_id = :user-a-id)

-- :name update-connection-status!
-- :command :execute
-- :result :affected
-- :doc Update the status for the connection matching the given user id's
UPDATE connected_users
SET    status = :status
WHERE  (user_sender_id = :user-a-id AND user_receiver_id = :user-b-id)
OR     (user_sender_id = :user-b-id AND user_receiver_id = :user-a-id)

-- :name all-connection-suggestions
-- :command :query
-- :result :many
-- :doc Selects connection suggestions by number of connections
SELECT registered_user.username as username,
       (SELECT COUNT(*)
        FROM connected_users
        WHERE user_sender_id = registered_user.id 
        OR user_receiver_id = registered_user.id) AS connections
FROM registered_user
WHERE registered_user.indexable=true 
AND NOT registered_user.id = :user-id
AND (SELECT COUNT(*) 
     FROM connected_users 
     WHERE (user_sender_id = registered_user.id AND user_receiver_id = :user-id) 
     OR (user_sender_id = :user-id AND user_receiver_id = registered_user.id)) = 0
ORDER BY connections DESC
LIMIT :limit OFFSET :offset