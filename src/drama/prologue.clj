(ns drama.prologue)

;; # Game of life :
;;
;; beauty of clojure in action
;; code available on https://github.com/laurentpetit/mixit2012
;; first version of it (classical with indexes)
;; http://clj-me.cgrand.net/2009/11/17/life-of-brian/
;; and final version some time later because simple ain't easy
;; http://clj-me.cgrand.net/2011/08/19/conways-game-of-life/

(defn neighbours [[x y]]
  (for [dx [-1 0 1] dy (if (zero? dx) [-1 1] [-1 0 1])]
    [(+ dx x) (+ dy y)]))

(defn step [cells]
  (set (for [[loc n] (frequencies (mapcat neighbours cells))
             :when (or (= n 3) (and (= n 2) (cells loc)))]
         loc)))

;; let's go step by step ...
; (def board #{[1 0] [1 1] [1 2]})
; #'user/board
; (take 5 (iterate step board))
; (#{[1 0] [1 1] [1 2]} #{[2 1] [1 1] [0 1]} #{[1 0] [1 1] [1 2]} #{[2 1] [1 1] [0 1]} #{[1 0] [1 1] [1 2]})