(ns drama.prologue)

;; # Data and Functions
;; ## bowling scorer
;;
;; inspired from http://www.objectmentor.com/resources/articles/xpepisode.htm
;; clojure solution publised by Stuart Halloway
;; too long skip it ?

(defn strike? [rolls]
  (= 10 (first rolls)))

(defn spare? [rolls]
  (and
    (= 10 (apply + (take 2 rolls)))
    (> 10 (first rolls))))

(defn frame-advance1
  "How many rolls should be consumed to advance to the next frame?"
  [rolls]
  (if (strike? rolls) 1 2))

(defn valid? [frame]
  (or (strike? frame)
      (spare? frame)
      (> 10 (reduce + frame))))

(defn balls-to-score [rolls]
  (cond
    (strike? rolls) 3
    (spare? rolls) 3
    :else 2))

(defn frame-advance [rolls]
  (cond
    (strike? rolls) 1
    :else 2))

(defn frames [rolls]
  {:post [(every? valid? %)]}
  (when-let [rolls (seq rolls)]
    (lazy-seq (cons (take (balls-to-score rolls) rolls)
                (frames (drop (frame-advance rolls) rolls))))))

(defn score [frames]
  (reduce + (map (partial reduce + ) frames)))

;; ## Game of life
;;
;; beauty of clojure in action
;; code totally stolen from https://github.com/laurentpetit/mixit2012
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