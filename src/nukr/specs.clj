(ns nukr.specs
  (:require
    [clojure.spec.alpha :as s]
    [clojure.test.check.generators]
    [clojure.spec.gen.alpha :as gen]))

(defn valid-jwt? [jwt]
  (re-matches #"^[a-zA-Z0-9\-_]+?\.[a-zA-Z0-9\-_]+?\.([a-zA-Z0-9\-_]+)?$" jwt))

(defn uuid-str-gen []
  (gen/fmap (fn [uuid] (.toString uuid)) (s/gen uuid?)))

(defn valid-uuid-str?
  "Ensures a match of the original uuid str with the result of coercing that str to
  and from a uuid"
  [uuid-str]
  (let [as-uuid (java.util.UUID/fromString uuid-str)]
    (= uuid-str (.toString as-uuid))))

(def non-empty-string-alphanumeric
  "Generator for non-empty alphanumeric strings"
  (gen/such-that #(not= "" %)
    (gen/string-alphanumeric)))

(def email-gen
  "Generator for email addresses"
  (gen/fmap
    (fn [[name host tld]]
      (str name "@" host "." tld))
    (gen/tuple
      non-empty-string-alphanumeric
      non-empty-string-alphanumeric
      non-empty-string-alphanumeric)))

(s/def ::message string?)
(s/def ::id
  (s/with-gen
    (s/and
      string?
      valid-uuid-str?)
    #(uuid-str-gen)))
(s/def ::username string?)
(s/def ::email
  (s/with-gen
    (s/and
      string?
      #(re-matches #".+@.+\..+" %))
    (fn [] email-gen)))
(s/def ::password (s/and string? #(< 7 (count %))))
(s/def ::permissions string?)
(s/def ::token
  (s/with-gen
    (s/and
      string?
      valid-jwt?)
    #(s/gen #{"J9.eyJ.5n"})))
(s/def ::refresh-token
  (s/with-gen
    (s/and
      string?
      valid-uuid-str?)
    #(uuid-str-gen)))
(s/def ::exp int?)
(s/def ::indexable boolean?)
(s/def ::status int?)

(s/def ::page int?)
(s/def ::per-page int?)
(s/def ::limit int?)
(s/def ::offset int?)

;; = Auth ======================================================================
(s/def ::auth-response (s/keys :req-un [::id ::username ::permissions ::token ::refresh-token]))
(s/def ::token-contents (s/keys :req-un [::id ::username ::email ::permissions ::exp]))

;; = User ======================================================================
(s/def ::register-request (s/keys :req-un [::username ::email ::password]))
(s/def ::register-response (s/keys :req-un [::username]))

;; = Patch User ================================================================
(s/def ::patch-pass-request (s/keys :req-un [::password]))
(s/def ::patch-pass-response (s/keys :req-un [::id ::username ::email ::indexable]))
(s/def ::change-username-request (s/keys :req-un [::username]))

;; = Request Password Reset ====================================================
(s/def ::useruser-email ::email)
(s/def ::from-email ::email)
(s/def ::subject string?)
(s/def ::email-body-html string?)
(s/def ::email-body-plain string?)
(s/def ::response-base-link string?)
(s/def ::request-reset-request (s/keys :req-un [::useruser-email ::from-email ::subject ::email-body-html ::email-body-plain ::response-base-link]))
(s/def ::request-reset-response (s/keys :req-un [::message]))

;; = Password Reset ============================================================
(s/def ::resetKey
  (s/with-gen
    (s/and
      string?
      valid-uuid-str?)
    #(uuid-str-gen)))
(s/def ::new-password ::password)
(s/def ::reset-request (s/keys :req-un [::resetKey ::new-password]))
(s/def ::reset-response (s/keys :req-un [::message]))

;; = Refresh Token =============================================================
(s/def ::refresh-token-response (s/keys :req-un [::token ::refresh-token]))
