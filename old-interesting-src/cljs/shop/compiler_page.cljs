(ns shop.compiler-page
  (:require [re-frame.core :as rf]
            [cljs.js :refer [eval-str empty-state js-eval]]))

(def default-code "(defn concentric-circles []\n  [:svg {:style {:border \"1px solid\"\n                 :background \"white\"\n                 :width \"150px\"\n                 :height \"150px\"}}\n   [:circle {:r 50, :cx 75, :cy 75, :fill \"red\"}]\n   [:circle {:r 25, :cx 100, :cy 100, :fill \"blue\"}]])\n\n[concentric-circles]")

(defn valid-hiccup? [vec]
  (let [first-element (nth vec 0 nil)]
    (cond
      (not (vector? vec)) false
      (not (pos? (count vec))) false
      (string? first-element) false
      (not (reagent.impl.template/valid-tag? first-element)) false
      (not (every? true? (map valid-hiccup? (filter vector? vec)))) false
      :else true)))

(defn compilation []
  (let [source-string @(rf/subscribe [:source])]
    (eval-str (empty-state)
              (str "(ns cljs.user  (:refer-clojure :exclude [atom]))"
                   (or (not-empty source-string)
                       "[:div]"))
              'user-code
              {:ns            'cljs.user
               :eval          js-eval
               :static-fns    true
               :def-emits-var false
               :load          (fn [_ cb]
                                (cb {:lang :clj :source ""}))
               :context       :statement}
              (fn [{:keys [error value] :as x}]
                (if error
                  (rf/dispatch [:set-error error])
                  (if (valid-hiccup? value)
                    (do
                      (rf/dispatch [:delete-error-message])
                      (rf/dispatch [:set-result value]))
                    (rf/dispatch [:set-error "Your hiccup is invalid"])))))))

(defn compiler-page []
  (when-not @(rf/subscribe [:source])
    (rf/dispatch-sync [:set-source default-code]))
  [:section.section>div.container>div.content
   [:textarea
    {:rows      40
     :cols      50
     :value     @(rf/subscribe [:source])
     :on-change #(rf/dispatch [:set-source (-> % .-target .-value)])}]
   [:div#result-pane @(rf/subscribe [:result])]
   [:div#error-pane {:style {:color "red"}} @(rf/subscribe [:error])]
   [compilation]])
