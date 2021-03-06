(ns midje-demo.t-core
  (:use midje.sweet)
  (:use [midje-demo.core]))

(facts "about `first-element`"
  (fact "it normally returns the first element"
    (first-element [1 2 3] :default) => 1
    (first-element '(1 2 3) :default) => 1)

  ;; I'm a little unsure how Clojure types map onto the Lisp I'm used to.
  (fact "default value is returned for empty sequences"
    (first-element [] :default) => :default
    (first-element '() :default) => :default
    (first-element nil :default) => :default
    (first-element (filter even? [1 3 5]) :default) => :default))
(facts "about `is-factorial?`"
  (fact "1 and the number itself are allways factorials"
    (is-factorial? 1 9) => true
    (is-factorial? 9 9) => true
    )
  (fact "divisors are factorials"
    (is-factorial? 2 9) => false
    (is-factorial? 3 9) => true
    )
  )
(facts "about `factorials`" 
  (fact "typical factorials"
    (factorials 6) => '(1 2 3 6)))
