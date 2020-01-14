(ns nukr.route-functions.user.connect-user
  (:require
    [ring.util.http-response :as respond]
    [nukr.query-defs :as query]
    [nukr.config :as c]))

(defn connect-user [current-user-id user-connectable-id]
  (let [connection (query/get-connection-by-user-ids query/db {:user-a-id current-user-id
                                                               :user-b-id user-connectable-id})]
    (cond 
      (empty? connection) (query/insert-connection! query/db {:user-sender-id current-user-id
                                                              :user-receiver-id user-connectable-id
                                                              :status (:pending c/connection-status)})
      (not-empty connection) (query/update-connection-status! query/db {:user-a-id current-user-id
                                                                        :user-b-id user-connectable-id
                                                                        :status (if (= (str (:user_receiver_id connection)) current-user-id) 
                                                                                      (:accept c/connection-status) 
                                                                                      (:pending c/connection-status))})))
      (respond/ok {:message "Connection request was successfully"}))


(defn connect-user-response [request username]
  (let [user-connectable (query/get-registered-user-by-username query/db {:username username})
        current-user-id (get-in request [:identity :id])]
    (cond
      (not-empty user-connectable) (connect-user current-user-id (:id user-connectable))
      (empty? user-connectable) (respond/not-found {:error "User does not exist"}))))