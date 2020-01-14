(ns nukr.routes.user
  (:require
    [clojure.spec.alpha :as s]
    [compojure.api.sweet :refer [context GET POST DELETE PATCH]]
    [nukr.specs :as specs]
    [nukr.middleware.cors :refer [cors-mw]]
    [nukr.middleware.token-auth :refer [token-auth-mw]]
    [nukr.middleware.authenticated :refer [authenticated-mw]]
    [nukr.route-functions.user.create-user :refer [create-user-response]]
    [nukr.route-functions.user.delete-user :refer [delete-user-response]]
    [nukr.route-functions.user.modify-user :refer [modify-user-response]]
    [nukr.route-functions.user.connect-user :refer [connect-user-response]]
    [nukr.route-functions.user.disconnect-user :refer [disconnect-user-response]]
    [nukr.route-functions.user.connection-suggestions :refer [get-connection-suggestions-response]]))


(s/def ::auth-header string?)

(def user-routes
  (context "/api/v1/user" []
           :tags ["User"]
           :coercion :spec

    (POST "/" {:as request}
           :return ::specs/register-response
           :middleware [cors-mw]
           :body-params [email :- ::specs/email
                         username :- ::specs/username
                         password :- ::specs/password]
           :summary "Create a new user with provided username, email and password."
           (create-user-response email username password))

    (DELETE "/:id" {:as request}
             :path-params [id :- ::specs/id]
             :return {:message ::specs/message}
             :header-params [authorization :- ::auth-header]
             :middleware [token-auth-mw cors-mw authenticated-mw]
             :summary "Deletes the specified user. Requires token to have `admin` auth or self ID."
             :description "Authorization header expects the following format 'Token {token}'"
             (delete-user-response request id))

    (PATCH  "/:id" {:as request}
             :path-params [id :- ::specs/id]
             :body-params [{username :- ::specs/username ""}
                           {password :- ::specs/password ""}
                           {email :- ::specs/email ""}
                           {indexable :- ::specs/indexable nil}]
             :header-params [authorization :- ::auth-header]
             :return ::specs/patch-pass-response
             :middleware [token-auth-mw cors-mw authenticated-mw]
             :summary "Update some or all fields of a specified user. Requires token to have `admin` auth or self ID."
             :description "Authorizationtrue header expects the following format 'Token {token}'"
             (modify-user-response request id username password email indexable))
    
    (GET    "/connection-suggestions" {:as request}
             :body-params [{page :- ::specs/page 1}
                           {per-page :- ::specs/per-page 20}]
             :return {:message ::specs/message
                      :limit ::specs/limit
                      :offset ::specs/offset}
             :header-params [authorization :- ::auth-header]
             :middleware [token-auth-mw cors-mw authenticated-mw]
             :summary "Get some or all connection suggestions"
             :description "Authorization header expects the following format 'Token {token}'"
             (get-connection-suggestions-response request page per-page))
            
    (PATCH  "/connect/:username" {:as request}
             :path-params [username :- ::specs/username]
             :return {:message ::specs/message}
             :header-params [authorization :- ::auth-header]
             :middleware [token-auth-mw cors-mw authenticated-mw]
             :summary "Connect your user to specific user. Requires username you want to connect."
             :description "Authorization header expects the following format 'Token {token}'"
             (connect-user-response request username))
    
    (PATCH  "/disconnect/:username" {:as request}
             :path-params [username :- ::specs/username]
             :return {:message ::specs/message}
             :header-params [authorization :- ::auth-header]
             :middleware [token-auth-mw cors-mw authenticated-mw]
             :summary "Disconnect your user to specific user. Requires username you want to connect."
             :description "Authorization header expects the following format 'Token {token}'"
             (disconnect-user-response request username))
  )
)
