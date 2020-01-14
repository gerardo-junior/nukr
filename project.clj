(defproject nukr "0.0.1"
  :description "nukr - The new business social media"

  :dependencies [[org.clojure/clojure "1.9.0"]
                 [metosin/compojure-api "2.0.0-alpha20"]
                 [metosin/spec-tools "0.7.0"]
                 [org.clojure/test.check "0.10.0-alpha2"]
                 [metosin/ring-http-response "0.9.0"]
                 [cheshire "5.8.0"]
                 [http-kit "2.3.0"]
                 [buddy "2.0.0"]
                 [org.clojure/java.jdbc "0.7.6"]
                 [org.postgresql/postgresql "9.4-1201-jdbc41"]
                 [com.layerware/hugsql "0.4.9"]
                 [environ "1.1.0"]
                 [migratus "1.0.6"]
                 [com.draines/postal "2.0.2"]
                 [ch.qos.logback/logback-classic "1.1.8" :exclusions [org.slf4j/slf4j-api]]
                 [org.slf4j/jul-to-slf4j "1.7.22"]
                 [org.slf4j/jcl-over-slf4j "1.7.22"]
                 [org.slf4j/log4j-over-slf4j "1.7.22"]]

  :plugins      [[lein-environ "1.1.0" :hooks false]
                 [migratus-lein "0.5.2"]
                 [funcool/codeina "0.5.0" :exclusions [org.clojure/clojure]]]

  :min-lein-version  "2.5.0"

  :resource-paths ["resources"]

  :migratus {:store :database
             :migration-dir "migrations"
             :db ~(get (System/getenv) "DATABASE_URL")}

  :uberjar-name "server.jar"

  :codeina {:sources ["src"]
            :reader :clojure}

  :profiles {:uberjar {:aot :all}

             ;; Set these in ./profiles.clj
             :test-env-vars {}
             :dev-env-vars  {}

             :test [:test-env-vars]
             :dev [{:dependencies [[ring/ring-mock "0.3.2"]]
                    :plugins [[jonase/eastwood "0.2.4"]]
                    :eastwood {:exclude-linters [:constant-test]
                               :include-linters [:deprecations]}}
                    :dev-env-vars]
             :production {:ring {:open-browser? false
                                 :stacktraces?  false
                                 :auto-reload?  false}}}

  :test-selectors {:default (constantly true)
                   :wip :wip})
