(ns nukr.route-functions.user.disconnect-user
  (:require
    [ring.util.http-response :as respond]
    [nukr.query-defs :as query]
    [nukr.config :as c]))


(defn disconnect-user [connection]
    (query/delete-connection! query/db {:user-a-id (:user_sender_id connection)
                                        :user-b-id (:user_receiver_id connection)})
    (respond/ok {:message "User successfully disconnected"}))

(defn disconnect-user-response [request username]
  (let [user-connectable (query/get-registered-user-by-username query/db {:username username})
        current-user (get-in request [:identity])
        connection (query/get-connection-by-user-ids query/db {:user-a-id (:id current-user)
                                                               :user-b-id (:id user-connectable)})]
    (cond
      (and (not-empty user-connectable) (not-empty connection)) (disconnect-user connection)
      (empty? user-connectable) (respond/not-found {:error "User does not exist"})
      (empty? connection) (respond/not-found {:error "Connection not found"}))))