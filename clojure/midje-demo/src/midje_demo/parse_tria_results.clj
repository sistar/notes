(ns midje-demo.parse-tria-results
  (:import [java.io FileNotFoundException])
  (:use hiccup.core)
  (:require [clojure-csv.core :as csv])
  (:require [clojure.string :as str])
  (:require [clojure.java.io :as io]))

(defn map-tag [tag xs]
  (map (fn [x] [tag x]) xs))



(defn out [sq]
  (def counter (atom 0))

  (html [:table [:tr (map-tag :th ["Platzierung" "Name" "Verein" "Jahrgang" "Gesamtzeit"])]
         (map
           (fn [m]
             (prn m)
              [:tr [:td (swap! counter inc)]
               [:td (m :name )]
               [:td (m :verein )]
               [:td (m :jg )]
               [:td (m :ziel-zeit )]]
              )
           sq)]))
(defn grabData [file]
  (str/split-lines (slurp file)))

(defn csv-seq-path [file]
  (map (fn [x] (first (csv/parse-csv x))) (grabData file)))

(defn grabDataFromFile [file process-fn]
  (with-open [rdr (clojure.java.io/reader file)]
    (doseq [line (line-seq rdr)]
      (process-fn line))))

(defn csv-seq [fname]
  (csv-seq-path (str "" fname)))

(defn map-flds [lsq]
  ;;(filter #(not (empty? (:ziel-zeit %)))
  (map #(zipmap [:platz,:startnummer,:name,:jg,:nat,:verein,:ak,:akp,:mw-platz,:ziel-zeit ] %) lsq)) ;;)

(defn agegroup-pred [yrs m] (contains? yrs (m :jg )))

(defn to-result-table
  ([lsq] (out (map-flds (rest lsq))))
  ([lsq pred] (out (filter pred (map-flds (rest lsq))))))

(defn in?
  "true if seq contains elm"
  [seq elm]
  (prn seq elm)
  (some #(= elm %) seq))

(defn second-line-last-col [seq]
  (last (second seq)))

(defn result-file [age-group gender]
  (let [f (clojure.java.io/file (str "Ergebnisliste-Kiezkindertriathlon 2013 " gender " " age-group " s.csv"))]
    (if (.exists f)
      f
      (throw (FileNotFoundException. (.getAbsolutePath f))))))

(def age-to-gender {"c" ["m" "w"]
                    "b" ["m" "w"]
                    "a" ["x"]
                    "j" ["x"]})

(defn swim-times [age-group]
  (map
    #(second-line-last-col (csv-seq-path (result-file age-group %)))
    (age-to-gender age-group)))

(defn first-str [times]
  (first (sort times)))

(defn min-swim-time [age-group]
  (first-str (swim-times age-group)))



