;; # Act I : extracting or scraping web pages with enlive
;;
;; The goal is to all plays and their characters
;; from some well-known writer (here a french one Molière)
;; (source : toutmoliere.net)
(ns drama.act1
  (:require  [net.cgrand.enlive-html :as h]))

;; ## Extract all plays
(def moliere "http://toutmoliere.net/")
#_(def raw (h/html-resource (java.net.URL. "http://toutmoliere.net/oeuvres.html")))
(def raw (h/html-resource (java.io.File. "resources/data/oeuvres.html")))

(def plays
  (let [nodes (h/select raw [:div#centre :div#liste1 :ul.listerub :li :a])
        extractor (fn [n]
                    {:url (str moliere (-> n :attrs :href))
                     :title (-> n :content first)
                     :date (-> n (h/select [:i]) first h/text)
                     })]
    (map extractor nodes)))

;; ## Extract the characters
(def a-play (h/html-resource (java.io.File. "resources/data/ecoledesfemmes.html")))



;; ## Put it all together

;; ## Further information
;;
;; wonderful world of automata : new version of enlive is coming
;; https://groups.google.com/group/enlive-clj/browse_thread/thread/5301234ebfaee3c4
;; https://groups.google.com/group/enlive-clj/browse_thread/thread/04730249c02c2e15
;;
;; small web app to play with enlive on the net
