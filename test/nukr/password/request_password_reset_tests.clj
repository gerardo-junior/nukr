(ns nukr.password.request-password-reset-tests
  (:import
    [java.time Instant]
    [java.time.temporal ChronoUnit])
  (:require
    [clojure.test :refer [use-fixtures deftest testing is]]
    [clojure.spec.alpha :as s]
    [clojure.spec.gen.alpha :as gen]
    [clojure.test.check.generators]
    [nukr.specs :as specs]
    [nukr.test-utils :as helper]
    [nukr.query-defs :as query]
    [nukr.route-functions.password.request-password-reset :as unit-test]))

(use-fixtures :once (fn [f]
                      (try
                        (query/insert-permission! query/db {:permission "basic"})
                        (helper/add-users)
                        (f)
                        (finally (query/truncate-all-tables-in-database! query/db)))))

(deftest test-html-email-body-returns-desired-string
  (testing "test add response link to html body returns desired string"
    (let [body "<html><body><p>Hello There</p></body></html>"
          response-link "http://somesite/reset/234"
          body-with-link (unit-test/html-email-body body response-link)]
      (is (= "<html><body><p>Hello There</p><br><p>http://somesite/reset/234</p></body></html>" body-with-link)))))

(deftest test-plain-email-body-returns-desired-string
  (testing "Test add response link to plain body reutrns desired string"
    (let [body "Hello there"
          response-link "http://somesite/reset/123"
          body-with-link (unit-test/plain-email-body body response-link)]
      (is (= "Hello there\n\nhttp://somesite/reset/123" body-with-link)))))

(deftest test-requesting-password-reset

  (testing "Successfully request password reset with email for a valid registered user"
    (with-redefs [unit-test/send-reset-email (fn [to-email from-email subject html-body plain-body] nil)]
      (let [user-id (:id (query/get-registered-user-by-username query/db {:username "steveJobs"}))
            reset-json (assoc (gen/generate (s/gen ::specs/request-reset-request)) :useruser-email "bill@microsoft.com")
            response (helper/non-authenticated-post "/api/v1/password/reset-request" reset-json)
            body (helper/parse-body (:body response))
            pass-reset-row (query/get-password-reset-keys-for-userid query/db {:userid user-id})
            valid-until-ts (:valid_until (first pass-reset-row))
            valid-until (.truncatedTo (.toInstant valid-until-ts) ChronoUnit/SECONDS)
            expected-time (.truncatedTo (.plus (Instant/now) 24 ChronoUnit/HOURS) ChronoUnit/SECONDS)]
        (is (= 200 (:status response)))
        (is (= 1 (count pass-reset-row)))
        (is (= valid-until expected-time))
        (is (= "Reset email successfully sent to bill@microsoft.com" (:message body))))))

  (testing "Invalid user email returns 404 when requesting password reset"
    (let [reset-json  (assoc (gen/generate (s/gen ::specs/request-reset-request)) :useruser-email "J@jrock.com")
          response (helper/non-authenticated-post "/api/v1/password/reset-request" reset-json)
          body (helper/parse-body (:body response))]
      (is (= 404 (:status response)))
      (is (= "No user exists with the email J@jrock.com" (:error body))))))
