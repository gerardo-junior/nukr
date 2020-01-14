(ns nukr.route-functions.refresh-token.delete-refresh-token
  (:require
    [ring.util.http-response :as respond]
    [nukr.query-defs :as query]))

(defn remove-refresh-token-response [refresh-token]
  (let [null-refresh-token (query/null-refresh-token! query/db {:refresh_token refresh-token})]
    (if (zero? null-refresh-token)
      (respond/not-found {:error "The refresh token does not exist"})
      (respond/ok {:message "Refresh token successfully deleted"}))))

