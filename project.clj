(defproject drama "0.1.0-SNAPSHOT"
  :description "A clojure drama in 3 acts and a prologue"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [ring "1.1.0"]
                 [enlive "1.0.1"]
                 [net.cgrand/moustache "1.1.0" :exclusions [ring]]
                 [cascalog "1.9.0"]]
  :profiles {:dev {:dependencies [[org.apache.hadoop/hadoop-core "0.20.2-dev"]]}})
