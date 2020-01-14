(ns nukr.routes.password
  (:require
    [compojure.api.sweet :refer [context POST]]
    [nukr.specs :as specs]
    [nukr.middleware.cors :refer [cors-mw]]
    [nukr.route-functions.password.password-reset :refer [password-reset-response]]
    [nukr.route-functions.password.request-password-reset :refer [request-password-reset-response]]))

(def password-routes
  (context "/api/v1/password" []
           :tags ["Password"]
           :return {:message ::specs/message}
           :middleware [cors-mw]
           :coercion :spec

    (POST "/reset-request" []
          :body-params [useruser-email :- ::specs/email
                        from-email :- ::specs/email
                        subject :- ::specs/subject
                        {email-body-html :- ::specs/email-body-html ""}
                        {email-body-plain :- ::specs/email-body-plain ""}
                        response-base-link :- ::specs/response-base-link]
          :summary "Request a password reset for the registered user with the matching email"
          :description "The `response-base-link` will get a reset key appended to it and then the
                        link itself will be appended to the email body. The reset key will be valid
                        for 24 hours after creation. *NOTE* do not use a from-email address ending
                        with @gmail.com because of the DMARC policy. It is recommended to use a custom
                        domain you own instead"
          (request-password-reset-response useruser-email from-email subject email-body-plain email-body-html response-base-link))

    (POST "/reset-confirm" []
           :body-params [resetKey :- ::specs/resetKey
                         new-password :- ::specs/new-password]
           :summary "Replace an existing user password with the newPassowrd given a valid resetKey"
           (password-reset-response resetKey new-password))))
