(ns midje-demo.core
	(:use [clojure.string :only (trim)])
	)
(defn first-element [sequence default]
  (if (empty? sequence)
    default
    (first sequence)))
(defn is-factorial? [factorial number]
	(=(mod number factorial) 0)
		)
(defn is-perfect-number [number]
	(= 1 number)
	)
;; starte mit 1
;; abbruch, wenn größer num


(defn calc-factorials [number act]
	(if (< act (inc number))
		(if (is-factorial? act number)
			(cons act (calc-factorials number (inc act)))
			(calc-factorials number (inc act))
		)
		(if (is-factorial? act number)
			(act)
			'()
			)))
	
(defn factorials [number]
	(calc-factorials number 1))