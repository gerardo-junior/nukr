(ns nukr.permission.permission-deletion-tests
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

(deftest can-delete-user-permission-with-valid-token-and-admin-permissions
  (testing "Can delete user permission with valid token and admin permissions"
    (helper/add-permission-for-username "steveJobs" "admin")
    (helper/add-permission-for-username "Everyman" "other")
    (let [user-id (:id (query/get-registered-user-by-username query/db {:username "Everyman"}))
          response (helper/authenticated-delete (str "/api/v1/permission/user/" user-id) {:permission "other"} "steveJobs:passwords")
          body (helper/parse-body (:body response))
          expected-response (str "Permission 'other' for user " user-id " successfully removed")]
      (is (= 200 (:status response)))
      (is (= "basic" (helper/get-permissions-for-user user-id)))
      (is (= expected-response (:message body))))))

(deftest can-not-delete-user-permission-with-valid-token-and-no-admin-permissions
  (testing "Can not delete user permission with valid token and no admin permissions"
    (helper/add-permission-for-username "steveJobs" "admin")
    (let [user-id (:id (query/get-registered-user-by-username query/db {:username "steveJobs"}))
          response (helper/authenticated-delete (str "/api/v1/permission/user/" user-id) {:permission "other"} "Everyman:passwords")
          body (helper/parse-body (:body response))]
      (is (= 401 (:status response)))
      (is (= "Not authorized" (:error body)))
      (is (= "basic,admin" (helper/get-permissions-for-user user-id))))))
