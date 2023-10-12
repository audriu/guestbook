(ns shop.core
  (:require
    [day8.re-frame.http-fx]
    [reagent.dom :as rdom]
    [reagent.core :as r]
    [reagent.session :as session]
    [re-frame.core :as rf]
    [goog.events :as events]
    [goog.history.EventType :as HistoryEventType]
    [markdown.core :refer [md->html]]
    [shop.ajax :as ajax]
    [shop.common :as c]
    [shop.events]
    [shop.survey :refer [survey-page]]
    [shop.compiler-page :refer [compiler-page]]
    [shop.things :refer [things-page]]
    [reitit.core :as reitit]
    [reitit.frontend.easy :as rfe]
    [clojure.string :as string])
  (:import goog.History))

(defn nav-link [uri title page]
  [:a.navbar-item
   {:href   uri
    :class (when (= page @(rf/subscribe [:page])) :is-active)}
   title])

(defn navbar []
  (r/with-let [expanded? (r/atom false)]
    [:nav.navbar.is-info>div.container
     [:div.navbar-brand
      [:a.navbar-item {:href "/" :style {:font-weight :bold}} "shop"]
      [:span.navbar-burger.burger
       {:data-target :nav-menu
        :on-click #(swap! expanded? not)
        :class (when @expanded? :is-active)}
       [:span][:span][:span]]]
     [:div#nav-menu.navbar-menu
      {:class (when @expanded? :is-active)}
      [:div.navbar-start
       [c/registration-button]
       [c/login-button]
       [nav-link "#/" "Home" :home]
       [nav-link "#/about" "About" :about]
       [nav-link "#/things" "Things" :things]
       [nav-link "#/compiler" "Compiler" :compiler]
       [nav-link "#/survey" "Survey" :survey]]]]))

(defn about-page []
  [:section.section>div.container>div.content
   [:img {:src "/img/warning_clojure.png"}]])

(defn home-page []
  [:section.section>div.container>div.content
   [:div "the main page content"]
   [:div  (str "User logged in: " (session/get :identity))]
   #_(when-let [docs @(rf/subscribe [:docs])]
     [:div {:dangerouslySetInnerHTML {:__html (md->html docs)}}])])

(defn modal []
  (case (session/get :modal)
    :register [c/registration-form]
    :login [c/login-form]
    nil))

(defn page []
  (if-let [page @(rf/subscribe [:page])]
    [:div
     [navbar]
     [modal]
     [page]]))

(defn navigate! [match _]
  (rf/dispatch [:navigate match]))

(def router
  (reitit/router
   [["/" {:name        :home
          :view        #'home-page
          :controllers [{:start (fn [_] (rf/dispatch [:page/init-home]))}]
          }]
    ["/things" {:name :things
                :view #'things-page}]
    ["/survey" {:name :survey
                :view #'survey-page}]
    ["/compiler" {:name :compiler
                :view #'compiler-page}]
    ["/about" {:name :about
               :view #'about-page}]]))

(defn start-router! []
  (rfe/start!
    router
    navigate!
    {}))

;; -------------------------
;; Initialize app
(defn mount-components []
  (rf/clear-subscription-cache!)
  (rdom/render [#'page] (.getElementById js/document "app")))

(defn init! []
  (start-router!)
  (ajax/load-interceptors!)
  (session/put! :identity js/identity)
  (mount-components))
