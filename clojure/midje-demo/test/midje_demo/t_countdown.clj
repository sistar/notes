(ns midje-demo.t-countdown 
	(:use midje.sweet)
	(:use [midje-demo.countdown]))
(facts "about `countdown`" 
  (fact "simple countdown"
    (countdown 6) => '(6 5 4 3 2 1)))