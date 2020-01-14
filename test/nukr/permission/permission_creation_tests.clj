(ns nukr.permission.permission-creation-tests
  (:require
    [clojure.test :refer [use-fixtures deftest testing is]]
    [nukr.test-utils :as helper]
    [nukr.query-defs :as query]))

(use-fixtures :each (fn [f]
                      (try
                        (query/insert-permission! query/db {:permission "basic"})
                        (query/insert-permission! query/db {:permission "admin"})
                        (query/insert-permission! query/db {:permission "other"})
                        (helper/add-users)
                        (f)
                        (finally (query/truncate-all-tables-in-database! query/db)))))

(deftest test-permission-creation

  (testing "Can add user permission with valid token and admin permissions"
    (helper/add-permission-for-username "steveJobs" "admin")
    (let [user-id (:id (query/get-registered-user-by-username query/db {:username "Everyman"}))
          response (helper/authenticated-post (str "/api/v1/permission/user/" user-id) {:permission "other"} "steveJobs:passwords")
          body (helper/parse-body (:body response))
          expected-response (str "Permission 'other' for user " user-id " successfully added")]
      (is (= 200 (:status response)))
      (is (= expected-response (:message body)))
      (is (= "basic,other" (helper/get-permissions-for-user user-id))))))

(deftest attempting-to-add-a-permission-that-does-not-exist-returns-404
  (testing "Attempting to add a permission that does not exist returns 404"
    (helper/add-permission-for-username "steveJobs" "admin")
    (let [user-id (:id (query/get-registered-user-by-username query/db {:username "Everyman"}))
          response (helper/authenticated-post (str "/api/v1/permission/user/" user-id) {:permission "stranger"} "steveJobs:passwords")
          body (helper/parse-body (:body response))]
      (is (= 404 (:status response)))
      (is (= "Permission 'stranger' does not exist" (:error body)))
      (is (= "basic" (helper/get-permissions-for-user user-id))))))

(deftest can-not-add-user-permission-with-valid-token-and-no-admin-permissions
  (testing "Can not add user permission with valid token and no admin permissions"
    (let [user-id  (:id (query/get-registered-user-by-username query/db {:username "Everyman"}))
          response (helper/authenticated-post (str "/api/v1/permission/user/" user-id) {:permission "other"} "Everyman:passwords")
          body (helper/parse-body (:body response))]
      (is (= 401 (:status response)))
      (is (= "Not authorized" (:error body)))
      (is (= "basic" (helper/get-permissions-for-user user-id))))))
