(defproject midje-demo "0.0.1-SNAPSHOT"
  :description "Cool new project to do things and stuff"
  :dependencies [	[org.clojure/clojure "1.5.1"]
  					[compojure "1.1.5"]
  					[hiccup "1.0.3"]
  					[clojure-csv/clojure-csv "2.0.0-alpha1"]]
  :plugins [[lein-midje "2.0.3"]
            [lein-ring "0.7.1" :exclusions [org.clojure/clojure]]]
  :profiles {:dev {:dependencies [[midje "1.5.1"]]}})
  
