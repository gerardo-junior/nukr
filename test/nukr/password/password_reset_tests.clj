(ns nukr.password.password-reset-tests
  (:require
    [clojure.test :refer [use-fixtures deftest testing is]]
    [clojure.spec.alpha :as s]
    [clojure.spec.gen.alpha :as gen]
    [clojure.test.check.generators]
    [buddy.hashers :as hashers]
    [nukr.specs :as specs]
    [nukr.test-utils :as helper]
    [nukr.query-defs :as query]))

(use-fixtures :once (fn [f]
                      (try
                        (query/insert-permission! query/db {:permission "basic"})
                        (helper/add-users)
                        (f)
                        (finally (query/truncate-all-tables-in-database! query/db)))))

(deftest test-password-resetting

  (testing "Test password is reset with valid resetKey"
    (let [user-id (:id (query/get-registered-user-by-username query/db {:username "steveJobs"}))
          reset-key (gen/generate (s/gen ::specs/resetKey))
          _ (query/insert-password-reset-key-with-default-valid-until! query/db {:reset_key reset-key :user_id user-id})
          response (helper/non-authenticated-post "/api/v1/password/reset-confirm" {:resetKey reset-key :new-password "new-pass"})
          body (helper/parse-body (:body response))
          updated-user (query/get-registered-user-by-id query/db {:id user-id})]
      (is (= 200 (:status response)))
      (is (= true (hashers/check "new-pass" (:password updated-user))))
      (is (= "Password successfully reset" (:message body)))))

  (testing "Not found 404 is returned when invalid reset key is used"
    (let [response (helper/non-authenticated-post "/api/v1/password/reset-confirm" {:resetKey (gen/generate (s/gen ::specs/resetKey)) :new-password "new-pass"})
          body (helper/parse-body (:body response))]
      (is (= 404 (:status response)))
      (is (= "Reset key does not exist" (:error body)))))

  (testing "Not found 404 is returned when valid reset key has expired"
    (let [reset-key (gen/generate (s/gen ::specs/resetKey))
          _ (query/insert-password-reset-key-with-valid-until-date! query/db {:reset_key reset-key
                                                                     :user_id (:id (query/get-registered-user-by-username query/db {:username "steveJobs"}))
                                                                     :valid_until (helper/create-offset-sql-timestamp :minus 24 :hours)})
          response (helper/non-authenticated-post "/api/v1/password/reset-confirm" {:resetKey reset-key :new-password "new-pass"})
          body (helper/parse-body (:body response))]
      (is (= 404 (:status response)))
      (is (= "Reset key has expired" (:error body)))))

  (testing "Password is not reset if reset key has already been used"
    (let [reset-key (gen/generate (s/gen ::specs/resetKey))
          user-id (:id (query/get-registered-user-by-username query/db {:username "steveJobs"}))
          _ (query/insert-password-reset-key-with-default-valid-until! query/db {:reset_key reset-key :user_id user-id})
          initial-response (helper/non-authenticated-post "/api/v1/password/reset-confirm" {:resetKey reset-key :new-password "new-pass"})
          second-response (helper/non-authenticated-post "/api/v1/password/reset-confirm" {:resetKey reset-key :new-password "not-gonna-happen"})
          body (helper/parse-body (:body second-response))
          updated-user (query/get-registered-user-by-id query/db {:id user-id})]
      (is (= 200 (:status initial-response)))
      (is (= 404 (:status second-response)))
      (is (= true (hashers/check "new-pass" (:password updated-user))))
      (is (= "Reset key already used" (:error body))))))
