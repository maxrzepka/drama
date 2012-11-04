;; # Act I : extracting or scraping web pages with enlive
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

#_(def raw (h/html-resource (java.net.URL. "http://toutmoliere.net/oeuvres.html")))

#_(def raw (h/html-resource (java.io.File. "resources/data/oeuvres.html")))

(defn extract-summary [page]
  (let [nodes (h/select (resource page) [:div#centre :div#liste1 :ul.listerub :li :a])
        extract (fn [n]
                    {:url (str moliere (-> n :attrs :href))
                     :title (-> n :content first)
                     :date (-> n (h/select [:i]) first  h/text)
                     })]
    (map extract nodes)))

;; ## Extract the characters
#_(def a-play (h/html-resource (java.io.File. "resources/data/ecoledesfemmes.html")))

(defn characters-url [page]
  (->> (h/select page [:ul#lapiece [:a (h/attr= :title "Acte 1")]])
       first
       :attrs
       :href
       (str moliere)
      ))

#_(def a-act1 (h/html-resource (java.io.File. "resources/data/ecoledesfemmes_acte1.html")))

(defn characters [nodes]
  (let [raw (:content (first (h/select nodes [:div#centre_texte :div :div])))
        raw1 (filter string? raw)
        extractor (fn[s] (map (fn[ss] (.trim ss))
                              (.split s ",")))]
    (map extractor raw1)))

;; ## Put it all together

;; lazy-sequence : fetch the data when requested
(defn all-in-one [plays]
  (map (fn [{u :url :as m}]
         (assoc m :characters (characters (resource (characters-url (resource u))))))
       plays))

(defn dump-file [f coll & {:keys [separator] :or {separator "|"}}]
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

;; ## Further information
;;
;; * wonderful world of automata : new version of enlive is coming
;; https://groups.google.com/group/enlive-clj/browse_thread/thread/5301234ebfaee3c4
;; https://groups.google.com/group/enlive-clj/browse_thread/thread/04730249c02c2e15
;;
;; * small web app to play with enlive API
