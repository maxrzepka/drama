;; # Act 2 : ask
;;
;; Let's play with data
;;
(ns drama.act2
  (:require [drama.act1 :as a1]
            [cascalog.api :as ca]
            [cascalog.ops :as co]
            ))

;; ## Model
;; play has title and date  , a character has name and desc

;; list of [title date]
(def plays
  (map vec (a1/file->coll "resources/data/moliere_plays.txt" )))

;; ## Some clean up
;; characters are some empty or last 2 columns should be join
;; drama.act2> ((juxt first count) (filter #(< 3 (count %)) characters))
;; [("La Jalousie du Barbouillé " "AGNÈS" "jeune fille innocente" "élevée par Arnolphe.") 99]
;; drama.act2> ((juxt first count) (filter #(> 3 (count %)) characters))
;; [("La Jalousie du Barbouillé ") 33]
;; drama.act2> ((juxt first count) (filter #(= 3 (count %)) characters))
;; [("La Jalousie du Barbouillé " "ARNOLPHE" "autrement M. DE LA SOUCHE.") 165]

;; list of [title name desc]
(def characters
  (keep (fn [r]
         (let [size (count r)]
           (cond (< 3 size) (let [[a b & c] r] [a b (clojure.string/join " " c)])
                 (= 3 size) (vec r)
                 :else nil)))
        (a1/file->coll "resources/data/moliere_characters.txt")))

;; ## Get all characters of a play
(defn find-characters [title]
  (ca/??<- [?name ?desc]
           (characters title ?name ?desc)))

;; ## Get all plays where a character is present
;; TODO sth wrong in the implicit join (all or nothing)
(defn find-plays [name]
  (ca/??<- [?title ?date]
           (plays ?title ?date)
           (characters ?title name ?desc)
           ))

;; ## Top 5 of the most used characters

