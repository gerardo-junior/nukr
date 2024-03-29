(ns nukr.general-functions.user.create-token
  (:require
    [environ.core :refer [env]]
    [buddy.sign.jwt :as jwt]))

(defn create-token
  "Create a signed json web token. The token contents are; username, email, id,
  permissions and token expiration time. Tokens are valid for 15 minutes."
  [user]
  (let [stringify-user (-> user
                           (update :username str)
                           (update :email str)
                           (assoc :exp (.plusSeconds (java.time.Instant/now) 900)))
        token-contents (select-keys stringify-user [:permissions :username :email :id :exp])]
    (jwt/sign token-contents (env :auth-key) {:alg :hs512})))
