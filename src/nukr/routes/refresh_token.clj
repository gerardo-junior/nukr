(ns nukr.routes.refresh-token
  (:require
    [compojure.api.sweet :refer [context GET DELETE]]
    [nukr.specs :as specs]
    [nukr.middleware.cors :refer [cors-mw]]
    [nukr.route-functions.refresh-token.delete-refresh-token :refer [remove-refresh-token-response]]
    [nukr.route-functions.refresh-token.gen-new-token :refer [gen-new-token-response]]))

(def refresh-token-routes
  (context "/api/v1/refresh-token/:refresh-token" []
           :tags ["Refresh-Token"]
           :coercion :spec
           :middleware [cors-mw]
           :path-params [refresh-token :- ::specs/refresh-token]

           (GET "/" request
                :return ::specs/refresh-token-response
                :summary "Get a fresh token and new refresh-token with a valid refresh-token."
                (gen-new-token-response refresh-token))

           (DELETE "/" request
                   :return {:message ::specs/message}
                   :summary "Delete the specific refresh-token"
                   (remove-refresh-token-response refresh-token))))
