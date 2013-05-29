(ns midje-demo.parse-tria-results
	(:use hiccup.core)
	(:require [clojure-csv.core :as csv])
	(:require [clojure.string :as str])
	(:require [clojure.java.io :as io]))

(defn map-tag [tag xs]
  (map (fn [x] [tag x]) xs))

(defn out [vec]
	(def counter (atom 0))
	(html [:table
	    [:tr (map-tag :th ["Platzierung" "Name" "Verein" "Jahrgang" "Gesamtzeit"])]
		
		    (for [[platz name verein jg ziel-zeit] vec]
			[:tr 
				[:td (swap! counter inc)] 
				[:td name]
				[:td verein]
				[:td jg]
				[:td ziel-zeit]])]))

(defn grabData [file]   
  (str/split-lines (slurp file)))

(defn csv-seq-path [file]
	(map (fn [x]  (first(csv/parse-csv x))) (grabData file) ))

(defn grabDataFromFile [file process-fn]   
  (with-open [rdr (clojure.java.io/reader file)]
  	(doseq [line (line-seq rdr)] 
      (process-fn line))))

(defn csv-seq [fname]
				(csv-seq-path (str "/home/mrbig/workspace/notes/clojure/midje-demo/" fname)))

(defn map-flds [lsq]
	(filter 
		(fn [v] (not(empty? (nth v 4))))
	(map 
		#(vector (nth % 8) (nth % 2) (nth % 5) (nth % 3) (nth % 9))
		 lsq)))

(defn to-result-table [lsq]
	(out(map-flds (rest lsq)))
	)

(defn in? 
  "true if seq contains elm"
  [seq elm]  
  (prn seq elm)
  (some #(= elm %) seq))

(defn second-line-last-col [seq]
	(last(second seq)))

(defn first-str [times]
	(first (sort times)))

(defn male-female-swim-times [age-group]
	(vector
		(second-line-last-col 
			(csv-seq-path (str "file:///home/mrbig/workspace/notes/clojure/midje-demo/Ergebnisliste-Kiezkindertriathlon%202013%20m%20" age-group "%20s.csv")))
		(second-line-last-col 
			(csv-seq-path (str "file:///home/mrbig/workspace/notes/clojure/midje-demo/Ergebnisliste-Kiezkindertriathlon%202013%20w%20" age-group "%20s.csv")))))

(defn min-swim-time [age-group]
	(first-str(male-female-swim-times age-group)))
(defn min-swim-time-x [age-group](second-line-last-col 
			(csv-seq-path (str "file:///home/mrbig/workspace/notes/clojure/midje-demo/Ergebnisliste-Kiezkindertriathlon%202013%20x%20" age-group "%20s.csv"))))

(defn to-result-table-as [lsq yrs]
	(out (filter  #(in? yrs (nth % 3)) (map-flds (rest lsq)))))

(defn to-result-tablex [lsq]
	(out (filter 
		(fn [v] (not(empty? (nth v 4))))
		(map #(vector (nth % 8) (nth % 2) (nth % 5) (nth % 3) (nth % 9))lsq))))


;;(grabDataFromFile "/home/mrbig/workspace/notes/clojure/midje-demo/in-file.csv" prn)