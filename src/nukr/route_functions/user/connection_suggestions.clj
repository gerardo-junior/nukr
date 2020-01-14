(ns nukr.route-functions.user.connection-suggestions
  (:require
    [ring.util.http-response :as respond]
    [nukr.query-defs :as query]))

(defn get-connection-suggestions [current-user-id page per-page]
  (let [connection-suggestions (query/all-connection-suggestions query/db {:user-id current-user-id
                                                                           :limit per-page
                                                                           :offset (* per-page page)})]
    (respond/ok {:message "Here is the list of connection suggestions" 
                 :limit per-page :offset (* per-page page)
                 :data (map #(.getValue (:username %) ) connection-suggestions)})))


(defn get-connection-suggestions-response [request page per-page]
  (let [current-user-id (get-in request [:identity :id])]
    (get-connection-suggestions current-user-id page per-page)))