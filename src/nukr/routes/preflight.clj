(ns nukr.routes.preflight
  (:require
    [compojure.api.sweet :refer [context OPTIONS]]
    [ring.util.http-response :as respond]
    [nukr.middleware.cors :refer [cors-mw]]))

(def preflight-route
  (context "/api" []
    :no-doc true

    (OPTIONS "*" {:as request}
              :tags ["Preflight"]
              :return {}
              :middleware [cors-mw]
              :summary "This will catch all OPTIONS preflight requests from the
                        browser. It will always return a success for the purpose
                        of the browser retrieving the response headers to validate CORS
                        requests."
              (respond/ok  {}))))
