(ns drama.epilogue)

;; # Game of life : Beauty of clojure in action
;;
;;
;; 1. code taken from https://gist.github.com/2491305 on https://github.com/laurentpetit/mixit2012
;; 2. [first version](http://clj-me.cgrand.net/2009/11/17/life-of-brian/) with indexes
;; 3. [final version](http://clj-me.cgrand.net/2011/08/19/conways-game-of-life/) some time later because simple ain't easy
;;
;; It's a classical example also present in the "Clojure Programming" book
;;

;; ## Logic
;;
;; The game is represented as a set of the living cells `#{[1 0] [1 1] [1 2]}`
;; mainly uses the swiss army knife of clojure toolkit : for
(defn neighbours [[x y]]
  (for [dx [-1 0 1] dy (if (zero? dx) [-1 1] [-1 0 1])]
    [(+ dx x) (+ dy y)]))

(defn step
  " let's go step by step ... in the REPL :`

 `(def board #{[1 0] [1 1] [1 2]})`
 
 `(take 5 (iterate step board))`
"
  [cells]
  (set (for [[loc n] (frequencies (mapcat neighbours cells))
             :when (or (= n 3) (and (= n 2) (cells loc)))]
         loc)))


;; ## GUI
;;
;; Just to show it live , the code himself is a good example of :
;;
;; 1. java interop
;; 2. atom simplest form of concurrency in clojure
;;
;; Instructions to run it :
;;
;; 1. `(swing-board board 5 5)` open the swing window with empty board
;; 2. `(play #{[1 0] [1 1] [1 2]} 100)` make it living
;; 3. `(def continue false)` stop GUI refresh and close swing window
;;
(defn str-board
  "A board is a plain string with h lines and each line contains w characters"
  [cells w h]
  (apply str (for [y (range h)
                   x (range (inc w))]
               (cond
                (= x w) \newline
                (cells [x y]) \O
                :else \.))))

(def board "Atom storing the current state of the game"
  (atom #{}))
(def sleep "Waiting time before compute next state"
  200)
(def poll-period "Refresh time" 40)

(def continue? "Set it at false to stop the game" true)

(defn swing-board
  "Display in a Swing TextArea, a board of size [w h] with living cells present in atom r .
   Every poll-period refresh the board.
"
  [r w h]
  (let [t (doto (javax.swing.JTextArea. "" h w)
            (.setFont (java.awt.Font/decode "Monospaced 48")))
        j (doto (javax.swing.JFrame. "Game of Life")
            (.add t)
            .pack
            .show)]
    (future (while continue?
              (Thread/sleep poll-period)
              (.setText t (str-board @r w h))))))


(defn play
  "Given the initial state of the board, compute the next n states (fct step)
and update the board every sleep period"
  [init n]
  (future
    (reset! board init)
    (dotimes [_ n]
      (Thread/sleep sleep)
      (swap! board step))))

