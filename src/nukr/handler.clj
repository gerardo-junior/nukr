(ns nukr.handler
  (:require
    [compojure.api.sweet :refer [api]]
    [ring.util.http-response :refer :all]
    [schema.core :as s]
    [nukr.routes.user :refer :all]
    [nukr.routes.preflight :refer :all]
    [nukr.routes.permission :refer :all]
    [nukr.routes.refresh-token :refer :all]
    [nukr.routes.auth :refer :all]
    [nukr.routes.password :refer :all]
    [nukr.middleware.basic-auth :refer [basic-auth-mw]]
    [nukr.middleware.token-auth :refer [token-auth-mw]]
    [nukr.middleware.cors :refer [cors-mw]]))

(def app
  (api
    {:swagger
      {:ui "/spec"
        :spec "/swagger.json"
        :data {:info {:title "nukr"
                      :description "nukr - The new business social media."}
               :tags [{:name "authenticated-api", :description ""}]
               :consumes ["application/json"]
               :produces ["application/json"]}}}
    preflight-route
    user-routes
    permission-routes
    refresh-token-routes
    auth-routes
    password-routes))
