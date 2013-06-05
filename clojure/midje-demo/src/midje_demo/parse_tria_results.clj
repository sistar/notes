(ns midje-demo.parse-tria-results
  (:import [java.io FileNotFoundException])
  (:use hiccup.core)
  (:require [clojure-csv.core :as csv])
  (:require [clojure.string :as str])
  (:require [clojure.java.io :as io])
  (:require [clj-http.client :as client]))

(def age-groups {2013 {"c" ["2005" "2004"] "b" ["2003" "2002"] "a" ["2001" "2000"] "sb" ["1999" "1998"]}})
(defn map-tag [tag xs]
  (map (fn [x] [tag x]) xs))

(defn call-verein-service [startnummer]
  (:verein (:body (client/get (str "http://localhost:8000/collection" "/" startnummer) {:accept :json :as :json}))))

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


(defn in?
  "true if seq contains elm"
  [seq elm]
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
(defn result-files-gender
  ([m] (result-files-gender (m :age ) (m :gender )))
  ([age-group gender]

    (rest (csv-seq-path (result-file age-group gender))
      ))
  )
(defn swim-times []
  (apply concat (map result-files-gender (for [age (keys age-to-gender) gender (age-to-gender age) :let [t {:age age :gender gender}]] t)))
  )
(defn startnummer-to-swimtimes []
  (apply merge (filter #(not (empty? (first (vals %)))) (map #(hash-map (second %) (last %)) (swim-times))))
  )
(defn rad-laufen-zeit [startnummer]

  )
(defn out [sq]
  (def counter (atom 0))

  (html [:table [:tr (map-tag :th ["Platzierung" "Name" "Verein" "Jahrgang" "Schwimmen" "Rad/Laufen" "Gesamtzeit"])]
         (map
           (fn [m]
             [:tr [:td (swap! counter inc)]
              [:td (m :name )]
              [:td (call-verein-service (m :startnummer ))]
              [:td (m :jg )]
              [:td ((startnummer-to-swimtimes) (m :startnummer ))]
              [:td (rad-laufen-zeit (m :startnummer ))]
              [:td (m :ziel-zeit )]]
             )
           sq)]))

(defn to-result-table
  ([lsq time-modifier] (out (map time-modifier (map-flds (rest lsq)))))
  ([lsq pred time-modifier] (out (map time-modifier(filter pred (map-flds (rest lsq)))))))

(defn best-swim-times
  ([age-group gender]
    (second-line-last-col (csv-seq-path (result-file age-group gender))))
  ([age-group]
    (map
      #(best-swim-times age-group %)
    (age-to-gender age-group))))

(defn first-str [times]
  (first (sort times)))

(defn min-swim-time
  ([age-group gender]
    (if (= (count (age-to-gender age-group)) 1)
      (first-str (best-swim-times age-group))
      (best-swim-times age-group gender)
      ))
  ([age-group]
    (first-str (best-swim-times age-group))))
(defn parse-int [s]
  (Integer. (re-find #"\d+" s)))
(defn add-timestr [a b]
  (if (or (empty? a) (empty? b))
    nil
    (let [[h m s] (map + (map parse-int (.split a ":")) (map parse-int (.split b ":")))]
      (let [carry-s-to-m (quot s 60)
            correct-s (mod s 60)
            carry-m-to-h (quot (+ m carry-s-to-m) 60)
            correct-m (mod (+ m carry-s-to-m) 60)
            correct-h (+ h carry-m-to-h)
            ]
        (format "%d:%02d:%02d" correct-h correct-m correct-s)))))

(defn add-fastest-swimtime-in-group [fastest-time-to-add-timestr m]
  (assoc m :ziel-zeit (add-timestr fastest-time-to-add-timestr (m :ziel-zeit))))

(def infile-keys {"c" "c",
                  "b" "b",
                  "a" "a",
                  "sb" "a"})
(defn generate-html-tables-for-age-key [year age-key]
  (prn ">>generating report for" year age-key)
  (doall (map (fn [gender-key]
                (let [mst (min-swim-time age-key)
                      time-modifier (partial add-fastest-swimtime-in-group mst)
                      infile-key (infile-keys age-key)
                      infile (format "in-file-%s-%s.csv" gender-key infile-key)
                      outfile (format "/tmp/%s-%s.html" gender-key age-key)
                      ]
                  (spit outfile
                    (to-result-table
                      (csv-seq infile)
                      year
                      age-key
                      time-modifier))))
           ["w" "m"])))
(defn generate-html-tables [year age-keys]
  (prn ">>age-keys" age-keys)
  (doall (map (fn [age-key] (generate-html-tables-for-age-key year age-key)) age-keys)))