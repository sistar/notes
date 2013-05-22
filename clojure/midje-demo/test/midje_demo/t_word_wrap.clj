(ns midje-demo.t-word-wrap
  (:use midje.sweet)
  (:use [midje-demo.word-wrap]))

(facts "about `last-index`"
  (fact "match"
    (last-index \s "this" ) => 4)
  (fact "no match"
    (last-index \s "this is simple"  ) => 9 ))
(facts "about `ùntil-last-space`"
  (fact "simple"
    (until-last-space "das ist") => "das "
    (until-last-space "das") => "das"
    ))
(facts "about `beginning-last-space-length`"
  (fact "simple"
    (beginning-last-space-length "das ist" 5) => "ist"
    (beginning-last-space-length "das" 5) => "das"
    ))

(facts "about `word wrap`"
  (fact "simple text"
    (wrap "thus us word" 14 ) => "thus us word")
  (fact "line break"
    (wrap "this is simple" 6 ) => "this\nis\nsimple"
    (wrap "this   is simple" 6 ) => "this\nis\nsimple"
    ))