(ns nukr.route-functions.password.password-reset
  (:require
    [buddy.hashers :as hashers]
    [ring.util.http-response :as respond]
    [nukr.query-defs :as query]))

(defn update-password [reset-key key-row new-password]
  (let [user-id (:user_id key-row)
        hashed-password (hashers/encrypt new-password)]
    (query/invalidate-reset-key! query/db {:reset_key reset-key})
    (query/update-registered-user-password! query/db {:id user-id :password hashed-password})
    (respond/ok {:message "Password successfully reset"})))

(defn password-reset-response [reset-key new-password]
  (let [key-row (query/get-reset-row-by-reset-key query/db {:reset_key reset-key})
        key-does-not-exist? (empty? key-row)
        key-valid? (when-let [valid-until (:valid_until key-row)]
                     (.isBefore (java.time.Instant/now) (.toInstant valid-until)))]
    (cond
      key-does-not-exist? (respond/not-found {:error "Reset key does not exist"})
      (:already_used key-row) (respond/not-found {:error "Reset key already used"})
      key-valid? (update-password reset-key key-row new-password)
      :else (respond/not-found {:error "Reset key has expired"}))))
