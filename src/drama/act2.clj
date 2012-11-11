;; # Act 2 : Let's play with data
;;
;; Cascalog is used to query our data. It's build on top Hadoop and cascading
;; but you don't need to have any knowlegde on Hadoop ecosystem or map/reduce in order to use it.
;;
;; Most of the time Cascalog lets you concentrate on "what" you want
;; instead of "how" : it's declarative like SQL.
;;
(ns drama.act2
  (:require [drama.act1 :as a1]
            [cascalog.api :as ca]
            [cascalog.ops :as co]
            ))

;; ## Model
;;
;; A play has a title and a date  , a character has a name and a description
;;
(def plays "list of [title date]"
  (map vec (a1/file->coll "resources/data/moliere_plays.txt" )))

(def  characters
  "List all records [title of the play, character's name , characters's desc ]
Clean up the raw file : skip empty records , merge columns if more than expected
"
  (keep (fn [r]
          (let [size (count r)]
            (cond (< 3 size) (let [[a b & c] r] [a b (clojure.string/join " " c)])
                  (= 3 size) (vec r)
                  :else nil)))
        (a1/file->coll "resources/data/moliere_characters.txt")))

;; ## Some cascalog queries
;;
;; Any cascalog query has always these 3 parts :
;;
;; 1. how to define and execute queries `<-` `?<-` `??<-`. [Here details](https://github.com/nathanmarz/cascalog/wiki/Defining-and-executing-queries)
;; 2. columns of the query
;; 3. predicates : generator , operation , aggregator . [Here details](https://github.com/nathanmarz/cascalog/wiki/Guide-to-custom-operations)
;;
(defn find-characters
  "Get All characters of a play"
  [title]
  (ca/??<- [?name ?desc]
           (characters title ?name ?desc)))

(defn find-plays
  "Get all the plays where a character is present : query using an implicit join"
  [name]
  (ca/??<- [?title ?date]
           (plays ?title ?date)
           (characters ?title name ?desc)))

(defn distinct-characters
  "List all character with their number of occurences in plays"
  []
  (ca/??<- [?name ?ct]
                  (characters ?title ?name ?desc)
                  (co/count ?ct)))

(defn top-n-characters
  "Get the n most used characters"
  [n]
  (let [count-q (ca/<- [?name ?ct]
                       (characters ?title ?name ?desc)
                       (co/count ?ct))
        q (co/first-n count-q n :sort ["?ct"] :reverse true)]
    (ca/??- q)))

;; ## Further topics
;;
;; 1. Files as Input / Output of queries
;; 2. More about joins 
;; 3. Create your own aggregate function
