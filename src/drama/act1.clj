;; # Act I : scraping web pages with enlive
;;
;; The goal is to get all plays and their characters
;; from some well-known writer (here a french one Molière)
;; (source : toutmoliere.net)
(ns drama.act1
  (:require  [net.cgrand.enlive-html :as h]))

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
                     :title (-> n :content first)
                     :date (-> n (h/select [:i]) first  h/text)
                     })]
    (map extract nodes)))

;; ## Extract the characters
;;
;; From play's main page  go to play's act 1 and extract list of characters
;; HTML page already present resources/data/ecoledesfemmes.html resources/data/ecoledesfemmes_acte1.html

(defn characters-url [page]
  (->> (h/select page [:ul#lapiece [:a (h/attr= :title "Acte 1")]])
       first
       :attrs
       :href
       (str moliere)
      ))

(defn characters [nodes]
  (let [raw (:content (first (h/select nodes [:div#centre_texte :div :div])))
        raw1 (filter string? raw)
        extractor (fn[s] (map (fn[ss] (.trim ss))
                              (.split s ",")))]
    (map extractor raw1)))

;; ## Put it all together

;; lazy-sequence : fetch the data when requested
(defn append-characters
  [plays]
  (map (fn [{u :url :as m}]
         (assoc m :characters (characters (resource (characters-url (resource u))))))
       plays))

;; ## Some IO functions
(defn dump-file
  [f coll & {:keys [separator] :or {separator "|"}}]
  (spit f (apply str (map #(str (clojure.string/join separator %) "\n") coll))))

(defn file->coll [f & {:keys [separator header] :or {separator "|"}}]
  (let [lines (.split (slurp f) "\n")
        separator ({"|" "\\|"} separator separator)
        cut (fn [l] ((if (sequential? header)
                       (partial zipmap header)
                       identity)
                     (seq (.split l separator))))]
    (map cut lines)))

(defn dump [plays]
  (let [l1 (map (juxt :title :date) plays)
        l2 (mapcat (fn [{cs :characters t :title :as p}]
                     (keep (fn [c] (when (< 1 (count c)) (cons t c))) cs))
                   plays)]
    (do (dump-file "resources/data/moliere_plays.txt" l1)
        (dump-file "resources/data/moliere_characters.txt" l2))))

;; ## Further information on enlive
;;
;; * https://github.com/cgrand/enlive
;; * small web app to play with enlive API http://cold-dusk-9608.herokuapp.com/
;;
;; * wonderful world of automata : new version of enlive is coming
;; https://groups.google.com/group/enlive-clj/browse_thread/thread/5301234ebfaee3c4
;; https://groups.google.com/group/enlive-clj/browse_thread/thread/04730249c02c2e15
;;
