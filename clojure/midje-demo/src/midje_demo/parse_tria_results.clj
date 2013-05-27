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
(defn csvParseData [file]
	(map (fn [x]  (first(csv/parse-csv x))) (grabData file) ))
(defn grabDataFromFile [file process-fn]   
  (with-open [rdr (clojure.java.io/reader file)]
  	(doseq [line (line-seq rdr)] 
      (process-fn line))))
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

(defn to-result-table-as [lsq yrs]
	
	(out (filter  #(in? yrs (nth % 3)) (map-flds (rest lsq)))))

(defn to-result-tablex [lsq]
	(out (filter 
		(fn [v] (not(empty? (nth v 4))))
		(map #(vector (nth % 8) (nth % 2) (nth % 5) (nth % 3) (nth % 9))lsq))))


;;(grabDataFromFile "/home/mrbig/workspace/notes/clojure/midje-demo/in-file.csv" prn)