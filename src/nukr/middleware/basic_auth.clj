(ns nukr.middleware.basic-auth
  (:require
    [buddy.auth.middleware :refer [wrap-authentication]]
    [nukr.auth-resources.basic-auth-backend :refer [basic-backend]]))

(defn basic-auth-mw
  "Middleware used on routes requiring basic authentication"
  [handler]
  (wrap-authentication handler basic-backend))
