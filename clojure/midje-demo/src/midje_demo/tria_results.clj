(ns midje-demo.tria-results
	(:use midje-demo.parse-tria-results)
	(:use hiccup.core))

(def gender-text {"w" "Deerns" "m" "Jungs"})
(def age-text {
	"c" "Altersklasse C Schüler"
	"b" "Altersklasse B Schüler"
	"a" "Altersklasse A Schüler"
	"sb" "Altersklasse B Jugend"})
(def distance-text {"c" "100 m Schwimmen, 2 km Radfahren, 400 m Laufen"
 	"b" "200 m Schwimmen, 5 km Radfahren, 1 km Laufen"
	"a" "400 m Schwimmen, 10 km Radfahren, 2,5 km Laufen"	
	"sb" "400 m Schwimmen, 10 km Radfahren, 2,5 km Laufen"})


(defn heading-results [age-key gender-key]
	(let [infile (format "/tmp/%s-%s.html" gender-key age-key)] 

		
	(prn age-key gender-key infile)
	(str (html [:h2 (gender-text gender-key) " " (age-text age-key)]
 			[:p (distance-text age-key)])
 		(slurp infile)
		))
)
(defn per-age-group [fn]
	(doall(map #(str (fn % "w") (fn % "m")) ["c" "b" "a" "sb"]))
	)
(defn per-gender [fn]
	(map #(fn %) ["w" "m"]))

(defn -main []
	(generate-html-tables 2013 '("c" "b" "a" "sb"))
	(let [r  (per-age-group heading-results)]
		(prn r)
		(spit "/tmp/tria-results.html" (clojure.string/join r))))