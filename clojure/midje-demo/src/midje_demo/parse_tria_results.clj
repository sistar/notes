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

(defn call-verein-service-raw [startnummer]
  (prn "calling verein-service for-startnummer" startnummer)
  (:verein (:body (client/get (str "http://localhost:8000/collection" "/" startnummer) {:accept :json :as :json}))))

(def call-verein-service
  (memoize call-verein-service-raw))

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
  (filter #(not (empty? (:rad-laufen-zeit %)))
  (map #(zipmap [:platz,:startnummer,:name,:jg,:nat,:verein,:ak,:akp,:mw-platz,:rad-laufen-zeit ] %) lsq)))

(defn in?
  "true if seq contains elm"
  [seq elm]
  (some #(= elm %) seq))

(defn agegroup-pred
  "filter for year of birth"
  [yrs-seq result-map]
  (let [year-of-birth (result-map :jg )]
    (prn yrs-seq year-of-birth (in? yrs-seq year-of-birth))
    (in? yrs-seq year-of-birth)))

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
                    "sb" ["x"]})

(defn result-files-gender
  ([m] (result-files-gender (m :age ) (m :gender )))
  ([age-group gender]

    (rest (csv-seq-path (result-file age-group gender))
      )))

(defn swim-times []
  (apply concat (map result-files-gender (for [age (keys age-to-gender) gender (age-to-gender age) :let [t {:age age :gender gender}]] t)))
  )

(defn startnummer-to-swimtimes []
  (apply merge (filter #(not (empty? (first (vals %)))) (map #(hash-map (second %) (last %)) (swim-times))))
  )
(def heading-strings ["Pl." "Name" "Verein" "JG" "Swim" "Rad/Run" "Gesamt"])
(def heading-hiccup (map-tag :th heading-strings))
(defn out [sq]
  (def counter (atom 0))
  (html [:table [:tr heading-hiccup]
         (map
           (fn [m]
             [:tr [:td (swap! counter inc)]
              [:td (m :name )]
              [:td (call-verein-service (m :startnummer ))]
              [:td (m :jg )]
              [:td ((startnummer-to-swimtimes) (m :startnummer ))]
              [:td (m :rad-laufen-zeit-korrigiert )]
              [:td (m :gesamt-zeit )]]
             )
           sq)]))

(defn to-result-table
  ([lsq time-modifier] (out (map time-modifier (map-flds (rest lsq)))))
  ([lsq pred time-modifier] (out (map time-modifier (filter pred (map-flds (rest lsq)))))))

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

(defn sub-timestr [a b]
  (if (or (empty? a) (empty? b))
    nil
    (let [[h m s] (map - (map parse-int (.split a ":")) (map parse-int (.split b ":")))]
      (prn h m s)
      (let [carry-s-to-m (if (< s 0) true false)
            correct-s (if carry-s-to-m (+ 60 s) s)
            carried-m (if carry-s-to-m (dec m) m)
            carry-m-to-h (if (< carried-m 0) true false)
            correct-m (if carry-m-to-h (+ 60 carried-m) carried-m)
            correct-h (if carry-m-to-h (dec h) h)
            ]
        (format "%d:%02d:%02d" correct-h correct-m correct-s)))))

(defn add-fastest-swimtime-in-group [fastest-time-to-add-timestr m]
  (let [gesamt-zeit (add-timestr fastest-time-to-add-timestr (m :rad-laufen-zeit ))
        schwimmzeit ((startnummer-to-swimtimes) (m :startnummer ))
        rad-laufen-zeit-korrigiert (sub-timestr gesamt-zeit schwimmzeit)]
    (prn rad-laufen-zeit-korrigiert gesamt-zeit schwimmzeit)
    (assoc m :gesamt-zeit gesamt-zeit
      :rad-laufen-zeit-korrigiert rad-laufen-zeit-korrigiert)
    ))

(def infile-keys {"c" "c",
                  "b" "b",
                  "a" "a",
                  "sb" "a"})

(defn generate-htm-table [gender-key year age-key]
  (let [mst (min-swim-time age-key)
        time-modifier (partial add-fastest-swimtime-in-group mst)
        infile-key (infile-keys age-key)
        infile (format "in-file-%s-%s.csv" gender-key infile-key)
        outfile (format "/tmp/%s-%s.html" gender-key age-key)
        age-groups-year (age-groups year)
        agegroup-pred-born (partial agegroup-pred (age-groups-year age-key))
        ]
    (prn ">>generating report for" infile "min swimtime " mst "age key" age-key)
    (spit outfile
      (to-result-table
        (csv-seq infile)
        agegroup-pred-born
        time-modifier))))

(defn generate-html-tables-for-age-key [year age-key]
  (doall (map #( generate-htm-table % year age-key) ["w" "m"])))

(defn generate-html-tables [year age-keys]
  (prn ">>age-keys" age-keys)
  (doall (map (fn [age-key] (generate-html-tables-for-age-key year age-key)) age-keys)))