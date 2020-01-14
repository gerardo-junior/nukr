(ns nukr.route-functions.password.request-password-reset
  (:require
    [clojure.string :as str]
    [environ.core :refer [env]]
    [postal.core :refer [send-message]]
    [ring.util.http-response :as respond]
    [nukr.query-defs :as query]))

(defn plain-email-body
  "Insert link into plaintext email body"
  [body response-link]
  (str body "\n\n" response-link))

(defn html-email-body
  "Insert link into HTML email body"
  [body response-link]
  (str (str/replace body #"</body></html>" "") "<br><p>" response-link "</p></body></html>"))

(defn send-reset-email
  "Send password reset email (uses Postal + https://account.sendinblue.com/)"
  [to-email from-email subject html-body plain-body]
  (send-message {:host "smtp-relay.sendinblue.com"
                 :user (env :sendinblue-user-login)
                 :port 587
                 :pass (env :sendinblue-user-password)}
                {:from from-email
                 :to to-email
                 :subject subject
                 :body [:alternative
                        {:type "text/plain" :content plain-body}
                        {:type "text/html"  :content html-body}]}))

(defn process-password-reset-request [user from-email subject email-body-plain email-body-html response-base-link]
  (let [reset-key (.toString (java.util.UUID/randomUUID))
        inserted-key (query/insert-password-reset-key-with-default-valid-until! query/db {:reset_key reset-key :user_id (:id user)})
        response-link (str response-base-link "/" (:reset_key inserted-key))
        body-plain (plain-email-body email-body-plain response-link)
        body-html (html-email-body email-body-html response-link)]
    (send-reset-email (str (:email user)) from-email subject body-html body-plain)
    (respond/ok {:message (str "Reset email successfully sent to " (str (:email user)))})))

(defn request-password-reset-response [user-email from-email subject email-body-plain email-body-html response-base-link]
  (let [user (query/get-registered-user-by-email query/db {:email user-email})]
    (if (empty? user)
      (respond/not-found {:error (str "No user exists with the email " user-email)})
      (process-password-reset-request user from-email subject email-body-plain email-body-html response-base-link))))
