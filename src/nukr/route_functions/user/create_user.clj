(ns nukr.route-functions.user.create-user
  (:require
    [buddy.hashers :as hashers]
    [ring.util.http-response :as respond]
    [nukr.query-defs :as query]))

(defn create-new-user [email username password]
  (let [hashed-password (hashers/encrypt password)
        new-user (query/insert-registered-user! query/db {:email email
                                                 :username username
                                                 :password hashed-password})]
    (query/insert-permission-for-user! query/db {:userid (:id new-user)
                                        :permission "basic"})
    (respond/created {} {:username (str (:username new-user))})))

(defn create-user-response [email username password]
  (let [username-query (query/get-registered-user-by-username query/db {:username username})
        email-query (query/get-registered-user-by-email query/db {:email email})
        email-exists? (not-empty email-query)
        username-exists? (not-empty username-query)]
    (cond
      (and username-exists? email-exists?) (respond/conflict {:error "Username and Email already exist"})
      username-exists? (respond/conflict {:error "Username already exists"})
      email-exists? (respond/conflict {:error "Email already exists"})
      :else (create-new-user email username password))))
