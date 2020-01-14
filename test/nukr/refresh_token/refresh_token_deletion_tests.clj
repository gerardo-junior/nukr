(ns nukr.refresh-token.refresh-token-deletion-tests
  (:require
    [clojure.test :refer [use-fixtures deftest testing is]]
    [clojure.spec.alpha :as s]
    [clojure.spec.gen.alpha :as gen]
    [clojure.test.check.generators]
    [ring.mock.request :as mock]
    [nukr.handler :refer [app]]
    [nukr.specs :as specs]
    [nukr.test-utils :as helper]
    [nukr.query-defs :as query]))

(use-fixtures :once (fn [f]
                      (try
                        (query/insert-permission! query/db {:permission "basic"})
                        (helper/add-users)
                        (f)
                        (finally (query/truncate-all-tables-in-database! query/db)))))

(deftest testing-refresh-token-deletion

  (testing "Can delete refresh token with valid refresh token"
    (let [user-id (:id (query/get-registered-user-by-username query/db {:username "steveJobs"}))
          initial-response (helper/basic-auth-get-request "/api/v1/auth" "steveJobs:passwords")
          initial-body (helper/parse-body (:body initial-response))
          refresh-token (:refresh-token initial-body)
          refresh-delete-response (app (mock/request :delete (str "/api/v1/refresh-token/" refresh-token)))
          body (helper/parse-body (:body refresh-delete-response))
          registered-user-row (query/get-registered-user-by-id query/db {:id user-id})]
      (is (= 200 (:status refresh-delete-response)))
      (is (= "Refresh token successfully deleted" (:message body)))
      (is (= nil (:refresh_token registered-user-row)))))

  (testing "Attempting to delete an invalid refresh token returns an error"
    (let [refresh-delete-response (app (mock/request :delete (str "/api/v1/refresh-token/" (gen/generate (s/gen ::specs/refresh-token)))))
          body (helper/parse-body (:body refresh-delete-response))]
      (is (= 404 (:status refresh-delete-response)))
      (is (= "The refresh token does not exist" (:error body))))))
