(ns nukr.routes.auth
  (:require
    [clojure.spec.alpha :as s]
    [compojure.api.sweet :refer [context GET]]
    [nukr.specs :as specs]
    [nukr.middleware.basic-auth :refer [basic-auth-mw]]
    [nukr.middleware.authenticated :refer [authenticated-mw]]
    [nukr.middleware.cors :refer [cors-mw]]
    [nukr.route-functions.auth.get-auth-credentials :refer [auth-credentials-response]]))


(s/def ::auth-header string?)

(def auth-routes
  (context "/api/v1/auth" []
           :coercion :spec

     (GET "/" {:as request}
           :tags ["Auth"]
           :return ::specs/auth-response
           :header-params [authorization :- ::auth-header]
           :middleware [basic-auth-mw cors-mw authenticated-mw]
           :summary "Returns auth info given a username and password in the '`Authorization`' header."
           :description "Authorization header expects '`Basic username:password`' where `username:password`
                         is base64 encoded. To adhere to basic auth standards we have to use a field called
                         `username` however we will accept a valid username or email as a value for this key."
           (auth-credentials-response request))))
