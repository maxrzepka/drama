;; # Act I : scraping web pages with enlive
;;
;; The goal is to get all plays and their characters
;; from some well-known writer (here the french Molière)
;;
;; (source : toutmoliere.net)
(ns drama.act1
  (:require  [net.cgrand.enlive-html :as h]
             [clojure.string :as s]))

;; ## Enlive selector
;;
;; Enlive is templating system working in the following lines :
;;
;; 1. plain HTML no special tags inside
;; 2. HTML page are converted to a tree of nodes like `{:tag :a :attrs {:href "/"} :content () }`
;; 3. enlive gives methods to select and transform this tree structure
;;
;; Web scraping with enlive are done in 2 steps :
;;
;; 1. use enlive selectors to find the part of HTML page containing wanted informations
;; 2. with normal function extract the infos from nodes structure

(defn resource
  "Small utility function to convert something into nodes"
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
;; Enlive selector are plain clj data structure giving a flexible to express your HTML selection
;;
;; The syntax can be at first sight a bit confusing but in fact driven by simple rules :
;;
;; 1. any selector is always inside a [] meaning inclusion
;; 2. inner [] means `and` for example in `[:li [:a (h/attr= :href "/")]]`
;; 3. Follows mostly CSS convention
;;
;; [More details](https://github.com/cgrand/enlive/wiki)
;;
(defn extract-plays
  "Extract list of plays from http://toutmoliere.net/oeuvres.html
in local resources/data/oeuvres.html
"
  [url]
  (let [nodes (h/select (resource url) [:div#centre :div#liste1 :ul.listerub :li :a])
        extract (fn [n]
                    {:url (str moliere (-> n :attrs :href))
                     :title (-> n :content first s/trim)
                     :date (-> n (h/select [:i]) first  h/text s/trim)
                     })]
    (map extract nodes)))

;; ## Extract the characters
;;
;; More involved logic : from play's main page  go to play's act 1 and then extract list of characters
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
;; TODO : can be improved
(defn extract-characters
  ""
  [nodes]
  (let [items (keep #(when-not (= :br (:tag %)) (h/text %))
                    (:content (first (h/select nodes [:div#centre_texte :div :div]))))
        trim (fn [s] (-> s
                         (s/replace-first #"^[,. ]+" "") ;trim left
                         (s/replace-first #"[,. ]+$" "") ;trim right
                         ))]
    (map (fn [l] (mapv trim (s/split l #",")))
         (s/split-lines (apply str items)))))

;; ## Put it all together

(defn append-characters
  "Associate to a play his characters"
  [{u :url :as play}]
  (let [curl (characters-url (resource u))
        chars (extract-characters (resource curl))]
    (assoc play
      :characters-url curl
      :characters chars)))

(defn all-in-one
  "Returns a lazy-sequence ie only fetch the data when requested"
  []
  (map append-characters
   (extract-plays "http://toutmoliere.net/oeuvres.html")))

;; ## Some IO functions

(defn coll->file
  [f coll & {:keys [separator] :or {separator "|"}}]
  (spit f (apply str (map #(str (s/join separator %) "\n") coll))))

(defn file->coll
  "Return an list of vectors by default and of maps if header given"
  [f & {:keys [separator header] :or {separator "|"}}]
  (let [lines (.split (slurp f) "\n")
        separator ({"|" "\\|"} separator separator)
        cut (fn [l] ((if (sequential? header)
                       (partial zipmap header)
                       identity)
                     (map #(.trim %) (.split l separator))))]
    (map cut lines)))

(defn plays->file [plays]
  (coll->file "resources/data/moliere_plays.txt"
             (map (juxt :title :date) plays)))

(defn characters->file [plays]
  (coll->file "resources/data/moliere_characters.txt"
             (mapcat (fn [{cs :characters t :title :as p}]
                     (keep (fn [c] (when (< 1 (count c)) (cons t c))) cs))
                     plays)))

;; ## Further information on enlive
;;
;; * Enlive on [github](https://github.com/cgrand/enlive)
;; * small web app to play with enlive API [on heroku](http://cold-dusk-9608.herokuapp.com/)
;;     source on [github](https://github.com/maxrzepka/clojure-by-example)
;; * wonderful world of automata : new version of enlive work-in-progress
;;   * [thread 1](https://groups.google.com/group/enlive-clj/browse_thread/thread/5301234ebfaee3c4)
;;   * [thread 2](https://groups.google.com/group/enlive-clj/browse_thread/thread/04730249c02c2e15)
;;
