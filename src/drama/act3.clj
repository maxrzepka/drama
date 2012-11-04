;; # Act 3 Back to the web
;;
;; * routing with moustache : in-depth into http://brehaut.net/blog/2011/ring_introduction
;; * HTML templating with enlive
(ns drama.act3
  (:use [net.cgrand.moustache :only [app delegate]]
        [ring.middleware.file :only [wrap-file]]
        [ring.util.response :only [response content-type]]
        [ring.middleware.stacktrace :only [wrap-stacktrace]]
        [ring.adapter.jetty :only [run-jetty]])
  (:require [net.cgrand.enlive-html :as h]
            [drama.act2 :as a2]))

(h/defsnippet list-item "list.html" [:div#main :ul :li]
  [{:keys [id text]}]
  [:a] (h/do-> (h/set-attr :href (str "/" id) ) (h/content id))
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
  {:id t :text d})

;; taken from git://github.com/myfreeweb/ringfinger.git
;; ringfinger /corefinger/src/main/clojure/corefinger/middleware.clj
(defn wrap-logging
  "simple logging handler
"
  [handler & {:keys [output keys-filter]
              :or {output :stdout
                   keys-filter [:status :uri :remote-addr :request-method]}}]
  (fn [req]
    (let [res (handler req)
          logger (cond
                  (= output :stdout) println
                  (= output :stderr) #(binding [*out* *err*]
                                        (println %))
                  (string? output) #(spit output (str % "\n") :append true)
                  (fn? output) output)
          entry (-> (select-keys (merge req res) keys-filter)
                    pr-str
                    (.replace "\\" ""))]
      (logger entry)
      res)))

#_(defn render [body]
  (let [body (if (sequential? body) (apply str body) body)]
    (response (clojure.java.io/input-stream (.getBytes body "utf-8")))))

(defn render
  "render view in utf-8"
  [body]
  (content-type
   (response body)
   "text/html ; charset=utf-8"))

;; To test your routes from the REPL :
;;  (routes {:uri "/" :request-method :get})
(def routes
  (app
   (wrap-stacktrace)
   (wrap-logging)
   (wrap-file "resources") ;; to get CSS files
   [""] (fn [req] (render (main "Molière Works" (map vec->item a2/plays))))
   [id &] (fn [req] (render (main id  (map vec->item (a2/find-characters id)))))))

(defn start [ & [port & options]]
  (run-jetty (var routes) {:port (or port 8080) :join? false}))

(defn -main []
  (let [port (try (Integer/parseInt (System/getenv "PORT"))
                  (catch  Throwable t 8080))]
    (start port)))
