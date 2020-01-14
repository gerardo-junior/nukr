(ns nukr.auth.credential-retrieval-tests
  (:require
    [clojure.spec.alpha :as s]
    [clojure.spec.gen.alpha :as gen]
    [clojure.test.check.generators]
    [clojure.test :refer [use-fixtures deftest testing is]]
    [environ.core :refer [env]]
    [buddy.sign.jwt :as jwt]
    [ring.mock.request :as mock]
    [nukr.handler :refer [app]]
    [nukr.test-utils :as helper]
    [nukr.query-defs :as query]
    [nukr.specs :as specs]))

(use-fixtures :once (fn [f]
                      (try
                        (query/insert-permission! query/db {:permission "basic"})
                        (query/insert-permission! query/db {:permission "admin"})
                        (helper/add-users)
                        (helper/add-permission-for-username "steveJobs" "admin")
                        (f)
                        (finally (query/truncate-all-tables-in-database! query/db)))))

(deftest credential-retrieval-tests

  (testing "Valid username and password return correct auth credentials"
    (let [response (helper/basic-auth-get-request "/api/v1/auth" "Everyman:passwords")
          body (helper/parse-body (:body response))
          token-contents (jwt/unsign (:token body) (env :auth-key) {:alg :hs512})]
      (is (= 200 (:status response)))
      (is (= "Everyman" (:username body)))
      (is (= "basic" (:permissions body)))
      (is (s/valid? ::specs/refresh-token (:refresh-token body)))
      (is (s/valid? ::specs/token-contents token-contents))))

  (testing "Valid email and password return correct auth credentials"
    (let [response (helper/basic-auth-get-request "/api/v1/auth" "e@man.com:passwords")
          body (helper/parse-body (:body response))
          token-contents (jwt/unsign (:token body) (env :auth-key) {:alg :hs512})]
      (is (= 200 (:status response)))
      (is (= "Everyman" (:username body)))
      (is (= "basic" (:permissions body)))
      (is (s/valid? ::specs/refresh-token (:refresh-token body)))
      (is (s/valid? ::specs/token-contents token-contents))))

  (testing "Multiple permissions are properly formated"
    (let [response (helper/basic-auth-get-request "/api/v1/auth" "steveJobs:passwords")
          body (helper/parse-body (:body response))]
      (is (= 200 (:status response)))
      (is (= "steveJobs" (:username body)))
      (is (s/valid? ::specs/refresh-token (:refresh-token body)))
      (is (= "basic,admin" (:permissions (jwt/unsign (:token body) (env :auth-key) {:alg :hs512}))))))

  (testing "Invalid password does not return auth credentials"
    (let [response (helper/basic-auth-get-request "/api/v1/auth" "steveJobs:badpass")
          body (helper/parse-body (:body response))]
      (is (= 401 (:status response)))
      (is (= "Not authorized" (:error body)))))

  (testing "Invalid username does not return auth credentials"
    (let [response (helper/basic-auth-get-request "/api/v1/auth" "badUser:passwords")
          body (helper/parse-body (:body response))]
      (is (= 401 (:status response)))
      (is (= "Not authorized" (:error body)))))

  (testing "No auth credentials are returned when no username and password provided"
    (let [response (helper/basic-auth-get-request "/api/v1/auth" "")
          body (helper/parse-body (:body response))]
      (is (= 401 (:status response)))
      (is (= "Not authorized" (:error body)))))

  (testing "User can generate a new tokens with a valid refresh-token"
    (let [initial-response (helper/basic-auth-get-request "/api/v1/auth" "steveJobs:passwords")
          initial-body (helper/parse-body (:body initial-response))
          refresh-token (:refresh-token initial-body)
          refreshed-response (app (mock/request :get (str "/api/v1/refresh-token/" refresh-token)))
          body (helper/parse-body (:body refreshed-response))
          token-contents (jwt/unsign (:token body) (env :auth-key) {:alg :hs512})]
      (is (= 200 (:status refreshed-response)))
      (is (not= refresh-token (:refresh-token body)))
      (is (s/valid? ::specs/refresh-token (:refresh-token body)))
      (is (s/valid? ::specs/token-contents token-contents))))

  (testing "Invalid refresh token does not return a new token"
    (let [response (app (mock/request :get (str "/api/v1/refresh-token/" (gen/generate (s/gen ::specs/refresh-token)))))
          body (helper/parse-body (:body response))]
      (is (= 400 (:status response)))
      (is (= "Bad Request" (:error body))))))
