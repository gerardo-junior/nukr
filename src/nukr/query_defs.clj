(ns nukr.query-defs
  (:require
    [hugsql.core :as hugsql]
    [environ.core :refer [env]]))

(def db (env :database-url))

(hugsql/def-db-fns "sql/truncate_all.sql")
(hugsql/def-db-fns "sql/user/password_reset_key.sql")
(hugsql/def-db-fns "sql/user/permission.sql")
(hugsql/def-db-fns "sql/user/registered_user.sql")
(hugsql/def-db-fns "sql/user/connected_users.sql")
(hugsql/def-db-fns "sql/user/user_permission.sql")
