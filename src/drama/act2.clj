;; # Act 2 : Let's play with data
;;
;; Cascalog is used to query our data. It's build on top Hadoop and cascading
;; but you don't need to have any knowlegde of Hadoop ecosystem or map/reduce in order to use it.
;;
;; Most of the time, Cascalog let's you concentrate on "what" you want
;; not on "how" : it's declarative like SQL.
;;
(ns drama.act2
  (:require [drama.act1 :as a1]
            [cascalog.api :as ca]
            [cascalog.ops :as co]))

;; ## Model
;; Data in cascalog are list of tuples
;;
(def plays "list of [title date url]"
  (a1/file->coll "resources/data/moliere_plays.txt" ))

(def  characters
  "List all records [title of the play, character's name , characters's desc ]
"
  (a1/file->coll "resources/data/moliere_characters.txt" :size 3))

;; ## Some cascalog queries
;; Any cascalog query has always these 3 parts :
;;
;; 1. How to define and execute queries `<-` `?<-` `??<-`. [Here details](https://github.com/nathanmarz/cascalog/wiki/Defining-and-executing-queries)
;; 2. Columns of the query
;; 3. Predicates : generator , operation , aggregator . [Here details](https://github.com/nathanmarz/cascalog/wiki/Guide-to-custom-operations)
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
           (plays ?title ?date ?url)
           (characters ?title name ?desc)))

(defn list-characters
  "List all characters with their number of occurences in plays"
  []
  (ca/??<- [?name ?ct]
           (characters ?title ?name ?desc)
           (co/count ?ct)))

(defn list-plays
  "List all plays and counting their characters"
  []
  (ca/??<- [?title ?date ?url ?ct]
           (plays ?title ?date ?url)
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
