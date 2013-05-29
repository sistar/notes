(ns midje-demo.t-parse-tria-results
	(:use midje.sweet)
	(:use midje-demo.parse-tria-results))
(facts "about out"
	(fact "it processes a sequence"
		(out [["1" "Karl" "fcsp" "2004" "01:03:02"]]) => "<table><tr><th>Platzierung</th><th>Name</th><th>Verein</th><th>Jahrgang</th><th>Gesamtzeit</th></tr><tr><td>1</td><td>Karl</td><td>fcsp</td><td>2004</td><td>01:03:02</td></tr></table>"
	))
(facts "about grabData"
	(fact "it reads a line"
		(second(let [path "/home/mrbig/workspace/notes/clojure/midje-demo/in-file-w-c.csv"]

		(grabData path))) => "1.,2,\"Lindner, Kira\",2004,DEU,VAF TriKids,,,1.,0:08:10" 
		))

(facts "about csvParseData"
	(fact "it reads a line"
		(second (csv-seq "in-file-w-c.csv")) => (list["1.","2","Lindner, Kira","2004","DEU","VAF TriKids","","","1.","0:08:10"] )
	))
(facts "about to-result-table"
	(fact "it works in isolation"
		(to-result-table (list["1.","2","Lindner, Kira","2004","DEU","VAF TriKids","","","1.","0:08:10"] ))
		=> "<table><tr><th>Platzierung</th><th>Name</th><th>Verein</th><th>Jahrgang</th><th>Gesamtzeit</th></tr><tr><td>1.</td><td>Lindner, Kira</td><td>VAF TriKids</td><td>2004</td><td>0:08:10</td></tr></table>"
		 )

	(fact "it generates a table"
		(spit "/tmp/w-c.html" (to-result-table (csv-seq "in-file-w-c.csv"))) => nil
		(spit "/tmp/m-c.html" (to-result-table (csv-seq "in-file-m-c.csv"))) => nil
		(spit "/tmp/w-b.html" (to-result-table (csv-seq "in-file-w-b.csv"))) => nil
		(spit "/tmp/m-b.html" (to-result-table (csv-seq "in-file-m-b.csv"))) => nil
	)
	(fact "it splits schüler a jugend b" 	
		(spit "/tmp/w-a.html" (to-result-table-as (csv-seq "in-file-w-a.csv") ["2000" "2001"])) => nil
		(spit "/tmp/m-a.html" (to-result-table-as (csv-seq "in-file-m-a.csv") ["2000" "2001"])) => nil
		(spit "/tmp/w-sb.html" (to-result-table-as (csv-seq "in-file-w-a.csv") ["1998" "1999"])) => nil
		(spit "/tmp/m-sb.html" (to-result-table-as (csv-seq "in-file-m-a.csv") ["1998" "1999"])) => nil
	))
	

(facts "about map-flds"
	(fact "my input vector"
		(second (csv-seq "in-file-w-c.csv")) => ["1." "2" "Lindner, Kira" "2004" "DEU" "VAF TriKids" "" "" "1." "0:08:10"]
		)
	(fact "it processes a sequence"
		(map-flds (vector(second (csv-seq "in-file-w-c.csv")))) => (list["1." "Lindner, Kira" "VAF TriKids" "2004" "0:08:10"])
	))		
(facts "about min-swim-time"
	(fact "it is able to read an ugly filename"
		(first (grabData "file:///home/mrbig/workspace/notes/clojure/midje-demo/Ergebnisliste-Kiezkindertriathlon%202013%20m%20c%20s.csv")) => "Platz,Nr.,Name,JG,Nat,Verein/*Ort,AK,AK-P,M/W-P,Zielzeit"
		)
	(fact ""
		(second-line-last-col 
			(csv-seq-path "file:///home/mrbig/workspace/notes/clojure/midje-demo/Ergebnisliste-Kiezkindertriathlon%202013%20m%20c%20s.csv")) => "0:02:40"
		)
	(fact "it finds the minimum swim time for an age group"
				(min-swim-time "c") => "0:02:28"
				(min-swim-time "b") => "0:03:24"
				(min-swim-time-x "a") => "0:05:56"
				(min-swim-time-x "j") => "0:07:25"
		))