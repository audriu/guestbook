(ns guestbook.routes.home
  (:require
   [guestbook.layout :as layout]
   [guestbook.db.core :as db]
   [guestbook.validation :refer [validate-message]]
   [clojure.java.io :as io]
   [guestbook.middleware :as middleware]
   [ring.util.response]
   [ring.util.http-response :as response]
   [struct.core :as st]))

(defn home-page [request]
  (layout/render
   request "home.html"))

(defn about-page [request]
  (layout/render request "about.html"))

(defn messages-page [{:keys [flash] :as request}]
  (layout/render request "messages.html" (merge {:messages (db/get-messages)}
                                                (select-keys flash [:name :message :errors]))))

(defn save-message! [{:keys [params]}]
  (if-let [errors (validate-message params)]
    (response/bad-request {:errors errors})
    (try
      (db/save-message! params)
      (response/ok {:status :ok})
      (catch Exception _
        (response/internal-server-error
         {:errors {:server-error ["Failed to save message!"]}})))))

(defn message-list [_]
  (response/ok {:messages (vec (db/get-messages))}))

(defn home-routes []
  [""
   {:middleware [middleware/wrap-csrf
                 middleware/wrap-formats]}
   ["/" {:get home-page}]
   ["/messages" {:get messages-page}]
   ["/message" {:post save-message!}]
   ["/about" {:get about-page}]])

