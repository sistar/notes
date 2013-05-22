(ns midje-demo.word-wrap
	(:use [clojure.string :only (trim)])
	)
	
(defn last-index [c string]
	(if (= c (.charAt string (dec(.length string))))
		(.length string)
		(if (>(.length string)1)
			(last-index c (.substring string 0 (dec(.length string))))
			-1)))

(defn tail [s n]
	(subs s n (.length s)))

(defn last-space-index [s]
	(last-index \space s))

(defn beginning-last-space [s]
	(tail s (last-space-index s)))

(defn subs-capped [s start-index end-index]
	(subs s start-index (min end-index (count s))))

(defn beginning-last-space-length [s length]
	(let [lsi (last-space-index (subs-capped s 0 length))]
	(if (= lsi -1)
		s
		(tail s lsi))))

(defn until-last-space [s]
	(let [lsi (last-space-index s)]
	(if (= lsi -1)
		s
		(subs s 0 lsi))))

(defn do_wrap [remainder length]
	(if (> (count remainder) length)
		(str (trim(until-last-space (subs remainder 0 length)))
			"\n" 
			(do_wrap (beginning-last-space-length 
				remainder length) length))
		remainder))
(defn wrap [text length]
	(do_wrap text length))
