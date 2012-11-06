;; # Act I : scraping web pages with enlive
;;
;; The goal is to get all plays and their characters
;; from some well-known writer (here a french one Molière)
;; (source : toutmoliere.net)
(ns drama.act1
  (:require  [net.cgrand.enlive-html :as h]
             [clojure.string :as s]))

(defn resource [path]
  (h/html-resource
   (if (.startsWith path "http:")
     (java.net.URL. path)
     (java.io.File. path))))

;; ## Extract all plays
(def moliere "http://toutmoliere.net/")

(defn extract-summary
  "Extract list of plays : url = http://toutmoliere.net/oeuvres.html"
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
;; From play's main page  go to play's act 1 and extract list of characters
;; For scraping HTML page already present at
;; resources/data/{ecoledesfemmes.html,ecoledesfemmes_acte1.html}

(defn characters-url [nodes]
  (->> (h/select nodes [:ul#lapiece [:a (h/attr= :title "Acte 1")]])
       first
       :attrs
       :href
       (str moliere)
      ))

(defn characters
  ""
  [nodes]
  (let [items (keep #(when-not (= :br (:tag %)) (h/text %))
                    (:content (first (h/select nodes [:div#centre_texte :div :div]))))
        trim (fn [s] (-> s
                         (s/replace-first #"^[,. ]+" "")
                         (s/replace-first #"[,. ]+$" "")))]
    (map (fn [l] (mapv trim (s/split l #",")))
         (s/split-lines (apply str items)))))

;; ## Put it all together

;;
(defn append-characters
  "Associate to a play his characters"
  [{u :url :as play}]
  (let [curl (characters-url (resource u))
        chars (characters (resource curl))]
    (assoc play
      :characters-url curl
      :characters chars)))

(defn all-in-one
  "Returns a lazy-sequence : only fetch the data when requested"
  []
  (map append-characters
   (extract-summary "http://toutmoliere.net/oeuvres.html")))

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
;; * https://github.com/cgrand/enlive
;; * small web app to play with enlive API http://cold-dusk-9608.herokuapp.com/
;;
;; * wonderful world of automata : new version of enlive is coming
;; https://groups.google.com/group/enlive-clj/browse_thread/thread/5301234ebfaee3c4
;; https://groups.google.com/group/enlive-clj/browse_thread/thread/04730249c02c2e15
;;
