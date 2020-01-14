(ns nukr.middleware.token-auth
  (:require
    [buddy.auth.middleware :refer [wrap-authentication]]
    [nukr.auth-resources.token-auth-backend :refer [token-backend]]))

(defn token-auth-mw
  "Middleware used on routes requiring token authentication"
  [handler]
  (wrap-authentication handler token-backend))
