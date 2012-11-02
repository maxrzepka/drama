;; # Act 3 Back to the web
;;
;; * routing with moustache : in-depth inro http://brehaut.net/blog/2011/ring_introduction
;; * HTML templating with enlive
(ns drama.act3
  (:use [net.cgrand.moustache :only [app delegate]]
        [ring.middleware.file :only [wrap-file]]
        [ring.adapter.jetty :only [run-jetty]])
  (:require [net.cgrand.enlive-html :as h]))


(defn prepend-attrs [att prefix]
  (fn[node] (update-in node [:attrs att] (fn[v] (str prefix v)))))

(h/deftemplate index "index.html" [title]
  [[:link (h/attr= :rel "stylesheet")]] (prepend-attrs :href "/")
  [:div :h3] (h/content title))

(def routes
  (app
   (wrap-file "resources")
   [""] (index "It's coming...")
   [id &] (fn [req] nil)))


(defn start [ & [port & options]]
  (run-jetty (var routes) {:port (or port 8080) :join? false}))


(defn -main []
  (let [port (try (Integer/parseInt (System/getenv "PORT"))
                  (catch  Throwable t 8080))]
    (start port)))
