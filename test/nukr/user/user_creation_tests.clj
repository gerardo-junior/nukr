(ns nukr.user.user-creation-tests
  (:import
    [java.time Instant]
    [java.time.temporal ChronoUnit])
  (:require
    [clojure.test :refer [use-fixtures deftest testing is]]
    [clojure.spec.alpha :as s]
    [clojure.spec.gen.alpha :as gen]
    [clojure.test.check.generators]
    [ring.mock.request :as mock]
    [cheshire.core :as ch]
    [nukr.specs :as specs]
    [nukr.handler :refer [app]]
    [nukr.test-utils :refer [parse-body]]
    [nukr.query-defs :as query]))

(defn create-user [user-map]
  (app (-> (mock/request :post "/api/v1/user")
           (mock/content-type "application/json")
           (mock/body (ch/generate-string user-map)))))

(defn assert-no-dup [user-1 user-2 expected-error-message]
  (let [_ (create-user user-1)
        response (create-user user-2)
        body (parse-body (:body response))]
    (is (= 409 (:status response)))
    (is (= 1 (count (query/all-registered-users query/db))))
    (is (= expected-error-message (:error body)))))

(use-fixtures :each (fn [f]
                      (try
                        (query/insert-permission! query/db {:permission "basic"})
                        (f)
                        (finally (query/truncate-all-tables-in-database! query/db)))))

(deftest can-successfully-create-a-new-user-who-is-given-basic-permission-as-default
  (testing "Can successfully create a new user who is given basic permission as default"
    (is (= 0 (count (query/all-registered-users query/db))))
    (let [rand-user (gen/generate (s/gen ::specs/register-request))
          response (create-user rand-user)
          body (parse-body (:body response))
          new-registered-user (query/get-registered-user-details-by-username query/db {:username (:username body)})
          registered-at (.truncatedTo (.toInstant (:created_on new-registered-user)) ChronoUnit/SECONDS)
          expected-time (.truncatedTo (Instant/now) ChronoUnit/SECONDS)]
      (is (= 201 (:status response)))
      (is (= 1 (count (query/all-registered-users query/db))))
      (is (= (:username rand-user) (:username body)))
      (is (= (:username rand-user) (str (:username new-registered-user))))
      (is (= expected-time registered-at))
      (is (= "basic" (:permissions new-registered-user))))))

(deftest can-not-create-a-user-if-username-already-exists-using-the-same-case
  (testing "Can not create a user if username already exists using the same case"
    (assert-no-dup {:email "bob@bob.com"   :username "bob" :password "pass5678" }
                   {:email "bob@Master.com"     :username "bob" :password "pass5678" }
                   "Username already exists")))

(deftest can-not-create-a-user-if-username-already-exists-using-mixed-case
  (testing "Can not create a user if username already exists using mixed case"
    (assert-no-dup {:email "jose@joao.com"   :username "jose" :password "pass5678" }
                   {:email "Jam@Master.com"     :username "jose" :password "pass5678" }
                   "Username already exists")))

(deftest can-not-create-a-user-if-email-already-exists-using-the-same-case
  (testing "Can not create a user if email already exists using the same case"
    (assert-no-dup {:email "jose@joao.com" :username "jose"   :password "the-first-pass" }
                   {:email "jose@joao.com" :username "jose01" :password "the-second-pass" }
                   "Email already exists")))

(deftest can-not-create-a-user-if-email-already-exists-using-mixed-case
  (testing "Can not create a user if email already exists using mixed case"
    (assert-no-dup {:email "wOnkY@email.com" :username "Jarrod" :password "Pass5678" }
                   {:email "WonKy@email.com" :username "josé"  :password "Pass5678" }
                   "Email already exists")))

(deftest can-not-create-a-user-if-username-and-email-already-exist-using-same-and-mixed-case
  (testing "Can not create a user if username and email already exist using same and mixed case"
    (assert-no-dup {:email "jose@email.com" :username "jose" :password "pass5678" }
                   {:email "jose@email.com" :username "jose" :password "pass5678" }
                   "Username and Email already exist")))
