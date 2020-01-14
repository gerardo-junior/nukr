(ns nukr.user.user-modify-tests
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

(deftest can-modify-a-users-username-with-valid-token-and-admin-permissions
  (testing "Can modify a users username with valid token and admin permissions"
    (helper/add-permission-for-username "steveJobs" "admin")
    (let [user-id (helper/get-id-for-user "steveJobs")
          response (helper/authenticated-patch (str "/api/v1/user/" user-id) {:username "billGates"} "steveJobs:passwords")
          body (helper/parse-body (:body response))]
      (is (= 200 (:status response)))
      (is (= "billGates" (:username body)))
      (is (= "bill@microsoft.com" (:email body))))))

(deftest can-modify-a-users-indexable-with-valid-token-and-admin-permissions
  (testing "Can modify a users indexable with valid token and admin permissions"
    (helper/add-permission-for-username "steveJobs" "admin")
    (let [user-id (helper/get-id-for-user "steveJobs")
          response (helper/authenticated-patch (str "/api/v1/user/" user-id) {:indexable false} "steveJobs:passwords")
          body (helper/parse-body (:body response))]
      (is (= 200 (:status response)))
      (is (= false (:indexable body)))
      (is (= "bill@microsoft.com" (:email body))))))

(deftest can-modify-a-users-email-with-valid-token-and-admin-permissions
  (testing "Can modify a users email with valid token and admin permissions"
    (helper/add-permission-for-username "steveJobs" "admin")
    (let [user-id (helper/get-id-for-user "steveJobs")
          response (helper/authenticated-patch (str "/api/v1/user/" user-id) {:email "steve@next.com"} "steveJobs:passwords")
          body (helper/parse-body (:body response))
          updated-user (query/get-registered-user-by-id query/db {:id user-id})]
      (is (= 200 (:status response)))
      (is (= "steveJobs" (:username body)))
      (is (= "steve@next.com" (:email body)))
      (is (= "steve@next.com" (str (:email updated-user)))))))

(deftest can-modify-a-users-password-with-valid-token-and-admin-permissions
  (testing "Can modify a users password with valid token and admin permissions"
    (helper/add-permission-for-username "steveJobs" "admin")
    (let [user-id (helper/get-id-for-user "steveJobs")
          response (helper/authenticated-patch (str "/api/v1/user/" user-id) {:password "newPasss"} "steveJobs:passwords")
          updated-user (query/get-registered-user-by-id query/db {:id user-id})]
      (is (= 200 (:status response)))
      (is (= true (hashers/check "newPasss" (:password updated-user)))))))

(deftest can-modify-your-own-password-with-valid-token-and-no-admin-permissions
  (testing "Can modify your own password with valid token and no admin permissions"
    (let [user-id (helper/get-id-for-user "steveJobs")
          response (helper/authenticated-patch (str "/api/v1/user/" user-id) {:password "newPasss"} "steveJobs:passwords")
          updated-user (query/get-registered-user-by-id query/db {:id user-id})]
      (is (= 200 (:status response)))
      (is (= true (hashers/check "newPasss" (:password updated-user)))))))

(deftest can-not-modify-a-user-with-valid-token-and-no-admin-permissions
  (testing "Can not modify a user with valid token and no admin permissions"
    (let [user-id (helper/get-id-for-user "Everyman")
          response (helper/authenticated-patch (str "/api/v1/user/" user-id) {:email "bad@mail.com"} "steveJobs:passwords")
          body (helper/parse-body (:body response))
          non-updated-user (query/get-registered-user-by-id query/db {:id user-id})]
      (is (= 401 (:status response)))
      (is (= "e@man.com" (str (:email non-updated-user))))
      (is (= "Not authorized" (:error body))))))

(deftest trying-to-modify-a-user-that-does-not-exist-return-a-404
  (testing "Trying to modify a user that does not exist returns a 404"
    (helper/add-permission-for-username "steveJobs" "admin")
    (let [user-id (gen/generate (s/gen ::specs/id))
          response (helper/authenticated-patch (str "/api/v1/user/" user-id) {:email "not@real.com"} "steveJobs:passwords")
          body (helper/parse-body (:body response))]
      (is (= 404 (:status response)))
      (is (= "Userid does not exist" (:error body))))))
