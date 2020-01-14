(ns nukr.test-utils
  (:import
    [java.sql Timestamp]
    [java.time Instant]
    [java.time.temporal ChronoUnit])
  (:require
    [cheshire.core :as ch]
    [ring.mock.request :as mock]
    [buddy.core.codecs :as codecs]
    [buddy.core.codecs.base64 :as b64]
    [nukr.handler :refer [app]]
    [nukr.query-defs :as query]))

(def str->base64 (comp codecs/bytes->str b64/encode))

(defn parse-body [body]
  (ch/parse-string (slurp body) true))

(defn basic-auth-header
  [request original]
  (mock/header request "Authorization" (str "Basic " (str->base64 original))))

(defn token-auth-header
  [request token]
  (mock/header request "Authorization" (str "Token " token)))

(defn get-user-token [username-and-password]
  (let [initial-response (app (-> (mock/request :get "/api/v1/auth")
                                  (basic-auth-header username-and-password)))
        initial-body (parse-body (:body initial-response))]
    (:token initial-body)))

(defn get-token-auth-header-for-user [request username-and-password]
  (token-auth-header request (get-user-token username-and-password)))

(defn basic-auth-get-request [url user-pass]
  (app (-> (mock/request :get url)
           (basic-auth-header user-pass))))

(defn non-authenticated-post [url body]
  (app (-> (mock/request :post url)
           (mock/content-type "application/json")
           (mock/body (ch/generate-string body)))))

(defn non-authenticated-patch [url body]
  (app (-> (mock/request :patch url)
           (mock/content-type "application/json")
           (mock/body (ch/generate-string body)))))

(defn non-authenticated-get [url body]
  (app (-> (mock/request :get url)
           (mock/content-type "application/json")
           (mock/body (ch/generate-string body)))))

(defn authenticated-get [url body user-pass]
  (app (-> (mock/request :get url)
           (mock/content-type "application/json")
           (mock/body (ch/generate-string body))
           (get-token-auth-header-for-user user-pass))))

(defn authenticated-post [url body user-pass]
  (app (-> (mock/request :post url)
           (mock/content-type "application/json")
           (mock/body (ch/generate-string body))
           (get-token-auth-header-for-user user-pass))))

(defn authenticated-patch [url body user-pass]
  (app (-> (mock/request :patch url)
           (mock/content-type "application/json")
           (mock/body (ch/generate-string body))
           (get-token-auth-header-for-user user-pass))))

(defn authenticated-delete [url body user-pass]
  (app (-> (mock/request :delete url)
           (mock/content-type "application/json")
           (mock/body (ch/generate-string body))
           (get-token-auth-header-for-user user-pass))))

(defn get-permissions-for-user [id]
  (:permissions (query/get-permissions-for-userid query/db {:userid id})))

(defn get-id-for-user [username]
  (:id (query/get-registered-user-by-username query/db {:username username})))

(defn add-permission-for-username [username permission]
  (let [user-id (:id (query/get-registered-user-by-username query/db {:username username}))]
    (query/insert-permission-for-user! query/db {:userid user-id :permission permission})))

(defn add-users []
  (let [user-1 {:email "bill@microsoft.com" :username "steveJobs" :password "passwords" }
        user-2 {:email "e@man.com" :username "Everyman" :password "passwords" }
        user-3 {:email "e@body.com" :username "Everybody" :password "passwords" :indexable false }]
    (app (-> (mock/request :post "/api/v1/user")
             (mock/content-type "application/json")
             (mock/body (ch/generate-string user-1))))
    (app (-> (mock/request :post "/api/v1/user")
             (mock/content-type "application/json")
             (mock/body (ch/generate-string user-2))))
    (app (-> (mock/request :post "/api/v1/user")
             (mock/content-type "application/json")
             (mock/body (ch/generate-string user-3))))))

(def chrno-map {:days ChronoUnit/DAYS
                :hours ChronoUnit/HOURS
                :minutes ChronoUnit/MINUTES
                :seconds ChronoUnit/SECONDS})
              
(def connection-status {:pending 0 
                        :accept 1})

(defn plus-minus-amt
  "Given an inst add or subtract the specified amount of units"
  [inst direction amt unit]
  (case direction
    :plus (.plus inst amt unit)
    :minus (.minus inst amt unit)))

(defn create-offset-sql-timestamp
  "Create a sql timestamp that is the specified amount of units in the specified direction
   Example usage: (create-sql-timestamp 10 :minus :minutes)"
  [direction amt unit]
    (-> (Instant/now)
        (plus-minus-amt direction amt (unit chrno-map))
        .toEpochMilli
        Timestamp.))
