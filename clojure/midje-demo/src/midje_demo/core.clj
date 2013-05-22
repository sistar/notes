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
	
(defn last-index [c string]
	(if (= c (.charAt string (dec(.length string))))
		(.length string)
		(if (>(.length string)1)
			(last-index c (.substring string 0 (dec(.length string))))
			-1)))

(defn tail [s n]
	(subs s n (.length s)))

(defn last-space [s]
	(last-index \space s))

(defn beginning-last-space [s]
	(tail s (last-space s)))

(defn subs-capped [s start-index end-index]
	(subs s start-index (min end-index (count s))))

(defn beginning-last-space-length [s length]
	(let [lsi (last-space (subs-capped s 0 length))]
	(if (= lsi -1)
		s
		(tail s lsi))))

(defn until-last-space [s]
	(let [lsi (last-space s)]
	(if (= lsi -1)
		s
		(subs s 0 lsi))))

(defn factorials [number]
	(calc-factorials number 1))


(defn do_wrap [remainder length]
	(if (> (count remainder) length)
		(str (trim(until-last-space (subs remainder 0 length)))
			"\n" 
			(do_wrap (beginning-last-space-length 
				remainder length) length))
		remainder))
(defn wrap [text length]
	(do_wrap text length))
