;; # Act 3 Back to the web
;;
;; Architecture in place :
;;
;; 1. HTML templating with enlive
;; 2. Routing with moustache : [in-depth intro](http://brehaut.net/blog/2011/ring_introduction)
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
;; It's based on 2 macros `defsnippet` and `deftemplate` both are defining a fct returning a sequence of strings
;;

(h/defsnippet list-item "list.html" [:div#main :ul :li]
  [{:keys [title text nolink]} ]
  [:a] (h/do-> (if nolink identity (h/set-attr :href (str "/" title) )) (h/content title))
  [:span] (h/content text))


(defn prepend-attrs [att prefix]
  (fn[node] (update-in node [:attrs att] (fn[v] (str prefix v)))))

(h/deftemplate main "list.html" [title items]
  [[:link (h/attr= :rel "stylesheet")]] (prepend-attrs :href "/")
  [:div#main :h3] (h/content title)
  [:div#main :ul] (if (and (sequential? items) (seq items))
                    (h/content (map list-item items))
                     (h/substitute "")))

(defn vec->item [[t d]]
  {:title t :text d})

(defn render
  "render view in utf-8"
  [body]
  (content-type
   (response body)
   "text/html ; charset=utf-8"))

;; ## Routing requests
;;
;; Ring is a perfect example of the motto "data and functions", it consists of
;;
;; 1. the request and response are the data
;; 2. handler returns a response given a request
;; 3. middleware is High-Order function : it takes a handler as first parameter
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

(def baked-routes
  "Here instead of running a cascalog query to get the list of characters, it gets the generated page"
  (app
   (wrap-file "resources")
   [""] (fn [req] (render (main "Molière Works" (map vec->item a2/plays))))
   [play &] (fn [req]
              (let [name (.substring ^String (url-decode (:uri req)) 1)]
                (file-response (str "/generated/" play ".html")
                               {:root "resources" :index-files? true :allow-symlinks? false})))))

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
