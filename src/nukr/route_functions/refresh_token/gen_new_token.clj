(ns nukr.route-functions.refresh-token.gen-new-token
  (:require
    [ring.util.http-response :as respond]
    [nukr.general-functions.user.create-token :refer [create-token]]
    [nukr.query-defs :as query]))

(defn create-new-tokens [user]
  (let [new-refresh-token (str (java.util.UUID/randomUUID))]
    (query/update-registered-user-refresh-token! query/db {:refresh_token new-refresh-token :id (:id user)})
    {:token (create-token user) :refresh-token new-refresh-token}))

(defn gen-new-token-response [refresh-token]
  (let [user (query/get-registered-user-details-by-refresh-token query/db {:refresh_token refresh-token})]
    (if (empty? user)
      (respond/bad-request {:error "Bad Request"})
      (respond/ok (create-new-tokens user)))))
