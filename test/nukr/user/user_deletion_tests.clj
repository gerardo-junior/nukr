(ns nukr.user.user-deletion-tests
  (:require
    [clojure.test :refer [use-fixtures deftest testing is]]
    [clojure.spec.alpha :as s]
    [clojure.spec.gen.alpha :as gen]
    [clojure.test.check.generators]
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

(deftest can-delete-user-who-is-not-self-and-associated-permissions-with-valid-token-and-admin-permissions
  (testing "Can delete user who is not self and associated permissions with valid token and admin permissions"
    (helper/add-permission-for-username "steveJobs" "admin")
    (is (= 2 (count (query/all-registered-users query/db))))
    (let [user-id (:id (query/get-registered-user-by-username query/db {:username "Everyman"}))
          response (helper/authenticated-delete (str "/api/v1/user/" user-id) {} "steveJobs:passwords")
          body (helper/parse-body (:body response))
          expected-response (str "User id " user-id " successfully removed")]
      (is (= 200 (:status response)))
      (is (= expected-response (:message body)))
      (is (= 1 (count (query/all-registered-users query/db))))
      (is (= nil (helper/get-permissions-for-user user-id))))))

(deftest can-delete-self-and-associated-permissions-with-valid-token-and-basic-permissions
  (testing "Can delete self and associated permissions with valid token and basic permissions"
    (is (= 2 (count (query/all-registered-users query/db))))
    (let [user-id (:id (query/get-registered-user-by-username query/db {:username "steveJobs"}))
          response (helper/authenticated-delete (str "/api/v1/user/" user-id) {} "steveJobs:passwords")
          body (helper/parse-body (:body response))
          expected-response (str "User id " user-id " successfully removed")]
      (is (= 200 (:status response)))
      (is (= expected-response (:message body)))
      (is (= 1 (count (query/all-registered-users query/db))))
      (is (= nil (helper/get-permissions-for-user user-id))))))

(deftest can-not-delete-user-who-is-not-self-with-valid-token-and-basic-permissions
  (testing "Can not delete user who is not self with valid token and basic permissions"
    (is (= 2 (count (query/all-registered-users query/db))))
    (let [user-id (:id (query/get-registered-user-by-username query/db {:username "Everyman"}))
          response (helper/authenticated-delete (str "/api/v1/user/" user-id) {} "steveJobs:passwords")
          body (helper/parse-body (:body response))]
      (is (= 401 (:status response)))
      (is (= "Not authorized" (:error body)))
      (is (= 2 (count (query/all-registered-users query/db)))))))

(deftest return-404-when-trying-to-delete-a-user-that-does-not-exists
  (testing "Return 404 when trying to delete a user that does not exists"
    (helper/add-permission-for-username "steveJobs" "admin")
    (let [response (helper/authenticated-delete (str "/api/v1/user/" (gen/generate (s/gen ::specs/id))) {} "steveJobs:passwords")
          body (helper/parse-body (:body response))]
      (is (= 404 (:status response)))
      (is (= "Userid does not exist" (:error body))))))
