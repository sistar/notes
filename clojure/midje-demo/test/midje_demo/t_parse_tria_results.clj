(ns midje-demo.t-parse-tria-results
  (:use midje.sweet)
  (:use hiccup.core)
  (:use midje-demo.parse-tria-results))
(def html-lindner-kiara (str "<table><tr>"
                          (html heading-hiccup)
                          "</tr><tr><td>1</td><td>Lindner, Kira</td><td>VAF TriKids</td><td>2004</td><td>0:02:28</td><td>0:08:10</td><td>0:10:38</td></tr></table>"))
(facts "about out"
  (fact "it processes a sequence"
    (out (sequence [{:platz "1.",:startnummer "2" ,:name "Lindner, Kira",
                     :jg "2004",:nat "DEU",:verein "VAF TriKids" ,:ak "" ,:akp "",
                     :mw-platz "1." ,:rad-laufen-zeit-korrigiert "0:08:10",
                     :gesamt-zeit "0:10:38"}])) => html-lindner-kiara
    ))

(facts "about `add-timestr`"
  (fact "adds without carry"
    (add-timestr "0:02:28" "0:02:28") => "0:04:56")
  (fact "adds with carry from seconds to minutes"
    (add-timestr "0:02:28" "0:02:58") => "0:05:26")
  (fact "adds with carry from seconds to minutes and hours"
    (add-timestr "1:59:59" "0:59:59") => "2:59:58"))

(facts "about grabData"
  (fact "it reads a line"
    (second (let [path "in-file-w-c.csv"]

              (grabData path))) => "1.,2,\"Lindner, Kira\",2004,DEU,VAF TriKids,,,1.,0:08:10"
    ))

(facts "about csvParseData"
  (fact "it reads a line"
    (second (csv-seq "in-file-w-c.csv")) => ["1.","2","Lindner, Kira","2004","DEU","VAF TriKids","","","1.","0:08:10"]
    ))
(facts "about to-result-table"
  (fact "it works in isolation"
    (to-result-table (list [] ["1.","2","Lindner, Kira","2004","DEU","VAF TriKids","","","1.","0:08:10"]) (partial add-fastest-swimtime-in-group (min-swim-time "c")))
    => html-lindner-kiara
    )

  (fact "it generates a table"
    (let [mst (min-swim-time "c")
          time-modifier (partial add-fastest-swimtime-in-group mst)]
      (spit "/tmp/w-c.html" (to-result-table (csv-seq "in-file-w-c.csv") time-modifier)) => nil
      (spit "/tmp/m-c.html" (to-result-table (csv-seq "in-file-m-c.csv") time-modifier)) => nil)
    (let [mst (min-swim-time "b")
          time-modifier (partial add-fastest-swimtime-in-group mst)]
      (spit "/tmp/w-b.html" (to-result-table (csv-seq "in-file-w-b.csv") time-modifier)) => nil
      (spit "/tmp/m-b.html" (to-result-table (csv-seq "in-file-m-b.csv") time-modifier)) => nil)
    )
  (fact "it splits schüler a jugend b"
    (let [s-a-pred (partial agegroup-pred ["2000" "2001"])
          j-b-pred (partial agegroup-pred ["1998" "1999"])
          mst-a (min-swim-time "a")
          mst-j (min-swim-time "sb")
          time-modifier-a (partial add-fastest-swimtime-in-group mst-a)
          time-modifier-j (partial add-fastest-swimtime-in-group mst-j)
          ]
      (spit "/tmp/w-a.html" (to-result-table (csv-seq "in-file-w-a.csv") s-a-pred time-modifier-a)) => nil
      (spit "/tmp/m-a.html" (to-result-table (csv-seq "in-file-m-a.csv") s-a-pred time-modifier-a)) => nil
      (spit "/tmp/w-jb.html" (to-result-table (csv-seq "in-file-w-a.csv") j-b-pred time-modifier-j)) => nil
      (spit "/tmp/m-jb.html" (to-result-table (csv-seq "in-file-m-a.csv") j-b-pred time-modifier-j)) => nil
      )))

(facts "about map-flds"
  (fact "my input vector"
    (second (csv-seq "in-file-w-c.csv")) => ["1." "2" "Lindner, Kira" "2004" "DEU" "VAF TriKids" "" "" "1." "0:08:10"]
    )
  (fact "it maps rows from vector to map with known keys"
    (first (map-flds (vector (second (csv-seq "in-file-w-c.csv"))))) => {:platz "1.",:startnummer "2" ,:name "Lindner, Kira",
                                                                         :jg "2004",:nat "DEU",:verein "VAF TriKids" ,:ak "" ,:akp "",
                                                                         :mw-platz "1." ,:rad-laufen-zeit "0:08:10"}
    )
  (fact "it processes a sequence"
    (first (map-flds (vector (second (csv-seq "in-file-w-c.csv"))))) => {:platz "1.",:startnummer "2" ,:name "Lindner, Kira",
                                                                         :jg "2004",:nat "DEU",:verein "VAF TriKids" ,:ak "" ,:akp "",
                                                                         :mw-platz "1." ,:rad-laufen-zeit "0:08:10"}))
(facts "about min-swim-time"
  (fact "result-file creates java.io.File according to naming conventions"
    (str (result-file "c" "m")) => "Ergebnisliste-Kiezkindertriathlon 2013 m c s.csv"
    )
  (fact "it is able to read an ugly filename"
    (first (grabData (clojure.java.io/file "Ergebnisliste-Kiezkindertriathlon 2013 m c s.csv"))) => "Platz,Nr.,Name,JG,Nat,Verein/*Ort,AK,AK-P,M/W-P,Zielzeit"
    )
  (fact ""
    (second-line-last-col
      (csv-seq-path (clojure.java.io/file "Ergebnisliste-Kiezkindertriathlon 2013 m c s.csv"))) => "0:02:40"
    )
  (fact "it finds the minimum swim time for an age group"
    (min-swim-time "c") => "0:02:28"
    (min-swim-time "b") => "0:03:24"
    (min-swim-time "a") => "0:05:56"
    (min-swim-time "sb") => "0:07:25"
    )
  (fact "it adds min-swim-time to total-time"
    (let [mst (min-swim-time "c")
          time-modifier (partial add-fastest-swimtime-in-group mst)
          lsq (csv-seq "in-file-w-c.csv")]
      (:gesamt-zeit (first (map time-modifier (map-flds (rest lsq)))))
      => "0:10:38")))
