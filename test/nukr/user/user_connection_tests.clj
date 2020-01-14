(ns nukr.user.user-connection-tests
  (:require
    [clojure.test :refer [use-fixtures deftest testing is]]
    [clojure.spec.alpha :as s]
    [clojure.spec.gen.alpha :as gen]
    [clojure.test.check.generators]
    [buddy.hashers :as hashers]
    [nukr.specs :as specs]
    [nukr.test-utils :as helper]
    [nukr.query-defs :as query]))

(use-fixtures :each (fn [f]
                      (try
                        (query/insert-permission! query/db {:permission "basic"})
                        (query/insert-permission! query/db {:permission "admin"})
                        (helper/add-users)
                        (f)
                        (finally (query/truncate-all-tables-in-database! query/db)))))

(deftest can-send-connect-request-with-username-and-token-valid
  (testing "Can send connect request username and token valid"
    (let [response (helper/authenticated-patch "/api/v1/user/connect/Everyman" {} "steveJobs:passwords")
          body (helper/parse-body (:body response))
          connection (query/get-connection-by-user-ids query/db {:user-a-id (helper/get-id-for-user "Everyman")
                                                                 :user-b-id (helper/get-id-for-user "steveJobs")})]
      (is (= 200 (:status response)))
      (is (not-empty connection))
      (is (= (:status connection) (:pending helper/connection-status))))))

(deftest can-accept-connect-request-with-username-and-token-valid
  (testing "Can accept connect request username and token valid"
    (helper/authenticated-patch "/api/v1/user/connect/Everyman" {} "steveJobs:passwords")
    (let [response (helper/authenticated-patch "/api/v1/user/connect/steveJobs" {} "Everyman:passwords")
          body (helper/parse-body (:body response))
          connection (query/get-connection-by-user-ids query/db {:user-a-id (helper/get-id-for-user "Everyman")
                                                                 :user-b-id (helper/get-id-for-user "steveJobs")})]
      (is (= 200 (:status response)))
      (is (not-empty connection))
      (is (= (:status connection) (:accept helper/connection-status))))))

(deftest can-connect-users-with-invalid-username-and-valid-token
  (testing "Can connect users with invalid username and valid token"
    (let [response (helper/authenticated-patch "/api/v1/user/connect/anything" {} "steveJobs:passwords")
          body (helper/parse-body (:body response))]
      (is (= 404 (:status response))))))

(deftest can-connect-users-with-username-and-token-invalid
  (testing "Can connect users username and token valid"
    (let [response (helper/non-authenticated-patch "/api/v1/user/connect/anything" {})
          body (helper/parse-body (:body response))]
      (is (= 401 (:status response))))))

(deftest can-disconnect-users-with-username-and-token-valid-with-connection
  (testing "Can disconnect users with username and token valid with connection"
    (helper/authenticated-patch "/api/v1/user/connect/Everyman" {} "steveJobs:passwords")
    (helper/authenticated-patch "/api/v1/user/connect/steveJobs" {} "Everyman:passwords")
    (let [response (helper/authenticated-patch "/api/v1/user/disconnect/Everyman" {} "steveJobs:passwords")
          body (helper/parse-body (:body response))
          connection (query/get-connection-by-user-ids query/db {:user-a-id (helper/get-id-for-user "Everyman")
                                                                 :user-b-id (helper/get-id-for-user "steveJobs")})]
      (is (= 200 (:status response)))
      (is (empty? connection)))))

(deftest can-disconnect-users-with-username-and-token-valid-but-wihout-connection
  (testing "Can disconnect users with username and token valid but wihout connection"
    (let [response (helper/authenticated-patch "/api/v1/user/disconnect/Everybody" {} "steveJobs:passwords")
          body (helper/parse-body (:body response))]
      (is (= 404 (:status response))))))

(deftest can-disconnect-users-with-username-valid-and-connection-but-with-invalid-token
  (testing "Can disconnect users with username valid and connection but with invalid token"
    (let [response (helper/non-authenticated-patch "/api/v1/user/disconnect/Everybody" {})
          body (helper/parse-body (:body response))]
      (is (= 401 (:status response))))))

(deftest can-get-connection-suggestions-with-token-valid
  (testing "Can get connection suggestions with token valid"
    (let [response (helper/authenticated-get "/api/v1/user/connection-suggestions" {:page 1 :per-page 20} "steveJobs:passwords")
          body (helper/parse-body (:body response))]
      (is (= 200 (:status response)))
      (is (vector? (:data body))))))

(deftest can-get-connection-suggestions-with-token-valid-but-invalid-params
  (testing "Can get connection suggestions with token valid but invalid params"
    (let [response (helper/authenticated-get "/api/v1/user/connection-suggestions" {:page "anything" :per-page "anything"} "steveJobs:passwords")
          body (helper/parse-body (:body response))]
      (is (= 400 (:status response))))))

(deftest can-disconnect-users-with-username-valid-and-connection-but-with-invalid-token
  (testing "Can disconnect users with username valid and connection but with invalid token"
    (let [response (helper/non-authenticated-get "/api/v1/user/connection-suggestions" {:page 1 :per-page 20})
          body (helper/parse-body (:body response))]
      (is (= 401 (:status response))))))

