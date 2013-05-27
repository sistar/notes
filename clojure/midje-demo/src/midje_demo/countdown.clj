(ns midje-demo.countdown
	(:use [clojure.string :only (trim)])
	)
(defn countup [num]
	(if(= 0 num)
		[]
		(conj (countup (dec num)) num)))
(defn countdown [num]
	 (rseq (countup num)))