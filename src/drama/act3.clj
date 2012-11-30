;; # Act 3 Back to the web
;;
;; Architecture in place :
;;
;; 1. Ring interface : 2 maps Request/Response and 2 functions handler/middleware
;; 2. Routing with moustache  [in-depth intro](http://brehaut.net/blog/2011/ring_introduction)
;; 3. HTML templating with enlive
;;
(ns drama.act3
  (:use [net.cgrand.moustache :only [app]]
        [ring.middleware.file :only [wrap-file]]
        [ring.util.codec :only [url-decode]]
        [ring.util.response :only [response content-type file-response]]
        [ring.adapter.jetty :only [run-jetty]])
  (:require [net.cgrand.enlive-html :as h]
            [drama.act2 :as a2]))


;; ## Enlive templating System
;;
;; It's based on 2 macros `defsnippet` and `deftemplate` both define a fct returning a sequence of strings
;;

(h/defsnippet list-item "list.html" [:div#main :ul :li]
  [{:keys [title text url nolink total]} ]
  [[:a h/first-of-type]]
  (h/do-> (if nolink identity (h/set-attr :href (str "/" title) ))
          (h/content title))
  [[:a (h/nth-of-type 2)]] (when url (h/set-attr :href  url))
  [[:span h/first-of-type]] (h/content text)
  [[:span (h/nth-of-type 2)]]  (h/content (str total)))


(defn prepend-attrs [att prefix]
  (fn[node] (update-in node [:attrs att] (fn[v] (str prefix v)))))

(h/deftemplate main "list.html" [title items]
  [[:link (h/attr= :rel "stylesheet")]] (prepend-attrs :href "/")
  [:div#main :h3] (h/content title)
  [:div#main :ul] (if (and (sequential? items) (seq items))
                    (h/content (map list-item items))
                    (h/substitute "")))

(defn vec->item [[t d u c]]
  {:title t :text d :url u :total c})

(defn render
  "render view in utf-8"
  [body]
  (content-type
   (response body)
   "text/html ; charset=utf-8"))

;; ## Routing requests
;; Ring is a perfect example of the motto "data and functions", it consists of
;;
;; 1. The request and response are the data
;; 2. Handler returns a response given a request
;; 3. Middleware is High-Order function : it takes a handler as first parameter
;; and returns a new handler function
;;
;; [More details](https://github.com/mmcgrana/ring/wiki/Concepts)
;;
;;  `routes` describes the behaviour of the web app : how to handle each incoming request.
;; `app` is the main function of moustache, it consists of 2 parts :
;;
;; 1. middlewares
;; 2. routes
;;
;; Test your routes from the REPL : `(routes {:uri \"/\" :request-method :get})`
;;
;; [More details](https://github.com/cgrand/moustache)
;;
(def routes
  (app
   (wrap-file "resources") ;; to get CSS files
   [""] (fn [req] (render (main "Molière Works" (map vec->item a2/plays))))
   [play &] (fn [req] (render (main play (map vec->item (a2/find-characters play)))))))

(defn generate-pages
  "Generates HTML pages for each play"
  []
  (doseq [[title _] a2/plays]
    (spit (str "resources/generated/" title ".html")
          (apply str (main title
                           (map #(assoc (vec->item %) :nolink 1)
                                (a2/find-characters title)))))))

(defn generate-summary
  []
  (spit (str "resources/generated/plays.html")
        (apply str (main "Molière Works" (map vec->item (a2/list-plays))))))

(defn baked-handler [name]
  (fn [req]
    (file-response
     (str name ".html")
     {:root "resources/generated" :index-files? true
      :allow-symlinks? false})))

(def baked-routes
  "Here instead of running a cascalog query to get the list of characters, it gets the generated page"
  (app
   (wrap-file "resources")
   [""] (baked-handler "plays")
   [play &] (baked-handler play)))

(defn start
  "Starts Jetty server with your routes.
Note `(var routes)` allows to do interactive web development
"
  [ & [port & options]]
  (run-jetty (var baked-routes) {:port (or port 8080) :join? false}))

(defn -main []
  (let [port (try (Integer/parseInt (System/getenv "PORT"))
                  (catch  Throwable t 8080))]
    (start port)))
