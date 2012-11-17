;; # Act I : Scraping web pages with enlive
;;
;; The goal is to retrieve all theater plays by a famous author (Here the french Molière from the 17th century)
;; and the characters from those plays.
;; (source : toutmoliere.net)
(ns drama.act1
  (:require  [net.cgrand.enlive-html :as h]
             [clojure.string :as s]))

;; ## Enlive selector
;;
;; Enlive is a templating system working as in the following lines :
;;
;; 1. plain HTML without any special tags
;; 2. HTML pages are converted to a tree of nodes, like `{:tag :a :attrs {:href "/"} :content () }`
;; 3. Enlive provides functions to select and transform the above mentioned tree structure.
;;
;; Web scraping with enlive are done in 2 steps :
;;
;; 1. Use enlive selectors to find the part of HTML page containing the requested information
;; 2. Common function extract the infos from the nodes

(defn resource
  "Converts a source (url , file or string) into nodes"
  [s]
  (let [r (cond
            (.startsWith s "http:") (java.net.URL. s)
            (.exists (java.io.File. s)) (java.io.File. s)
            :else s)]
    (if (not (string? r)) (h/html-resource r) (h/html-snippet r))))

(def moliere "http://toutmoliere.net/")

;; ## Extract all plays
;; Typical scraping structure is done in 2 steps : select and extract
;;
;; Enlive selectors are a flexible way to express your HTML selection
;;
;; The syntax can be at first sight a bit confusing, but in fact following simple rules :
;;
;; 1. any selector is always inside a [] . In this case [] means inclusion
;; 2. inner [] means `and` for example in `[:li [:a (h/attr= :href "/")]]`
;; 3. Follows CSS syntax
;;
;; [More details](https://github.com/cgrand/enlive/wiki)

(defn extract-plays
  "Extracts the list of plays by the author from http://toutmoliere.net/oeuvres.html
in local resources/data/oeuvres.html
"
  [url]
  (let [nodes (h/select (resource url) [:div#liste1 :ul.listerub :li :a])
        extract (fn [n]
                    {:url (str moliere (-> n :attrs :href))
                     :title (-> n :content first s/trim)
                     :date (-> n (h/select [:i]) first  h/text s/trim)
                     })]
    (map extract nodes)))

;; ## Extract the characters
;; Involves a more complex logic : from the play's main page, go to play's act 1 page and then extract the list of characters from there.
;;
;; 2 samples pages are available in local :
;; resources/data/{ecoledesfemmes.html,ecoledesfemmes_acte1.html}

(defn characters-url
  "Extract url of Acte 1"
  [nodes]
  (->> (h/select nodes [:ul#lapiece [:a (h/attr= :title "Acte 1")]])
       first
       :attrs
       :href
       (str moliere)
      ))

;;
;; A bit more tricky here, HTML pages are not always well structured
;;
(defn extract-characters
  "Returns a list of vectors [name , description]
"
  [nodes]
  (let [items (h/select
               nodes
               [:div#centre_texte :div :div h/text-node])
        items (s/split-lines (apply str items)) ;;one line = one character
        trim (fn [s] (-> s
                         (s/replace-first #"^[,. ]+" "") ;trim left
                         (s/replace-first #"[,. ]+$" "") ;trim right
                         ))]
    ;;first comma separates the character's name from the description
    (map (fn [l] (mapv trim (s/split l #"," 2))) items)))

;; ## Put it all together
;;
(defn append-characters
  "Associate the characters to a play"
  [{u :url :as play}]
  (let [curl (characters-url (resource u))
        chars (extract-characters (resource curl))]
    (assoc play
      :characters-url curl
      :characters chars)))

(defn all-in-one
  "Returns all informations wanted as a lazy-sequence
ie only fetch the data when requested.

Please use it with caution as it scrapes more than 60 web pages.
"
  []
  (map append-characters
   (extract-plays "http://toutmoliere.net/oeuvres.html")))

;; ## Some IO functions

(defn coll->file
  [f coll & {:keys [separator] :or {separator "|"}}]
  (spit f (apply str (map #(str (s/join separator %) "\n") coll))))

(defn file->coll
  "Returns a list of vectors. If the header is supplied, it returns a list of maps"
  [f & {:keys [separator header] :or {separator "|"}}]
  (let [lines (.split (slurp f) "\n")
        separator ({"|" "\\|"} separator separator)
        cut (fn [l] ((if (sequential? header)
                       (partial zipmap header)
                       identity)
                     (map #(.trim %) (.split l separator))))]
    (map cut lines)))

(defn plays->file
  "Loads into resources/data/moliere_plays.txt all plays."
  [plays]
  (coll->file "resources/data/moliere_plays.txt"
             (map (juxt :title :date) plays)))

(defn characters->file
  "Loads into resources/data/moliere_characters.txt all characters.
Skip invalid characters"
  [plays]
  (let [valid? (fn [c] (and (< 1 (count c))
                            (= (first c) (.toUpperCase (first c)))))]
    (coll->file "resources/data/moliere_characters.txt"
                      (mapcat (fn [{cs :characters t :title}]
                                (keep (fn [c] (when (valid? c) (cons t c)))
                                      cs))
                              plays))))

;; ## Further information on enlive
;;
;; * Enlive on [github](https://github.com/cgrand/enlive)
;; * small web app to play with enlive API [on heroku](http://cold-dusk-9608.herokuapp.com/)
;;     source on [github](https://github.com/maxrzepka/clojure-by-example)
;; * wonderful world of automata : new version of enlive work-in-progress
;;   * [thread 1](https://groups.google.com/group/enlive-clj/browse_thread/thread/5301234ebfaee3c4)
;;   * [thread 2](https://groups.google.com/group/enlive-clj/browse_thread/thread/04730249c02c2e15)
;;
