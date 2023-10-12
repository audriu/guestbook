(ns shop.survey
  (:require [reagent.core :as r]))

(defn transform-text [val]
  (apply str (take (count val) (cycle "I am an idiot!!! "))))

(def text (r/atom ""))
(def new-text (reagent.ratom/reaction (transform-text @text)))

(defn survey-page []
  [:section.section>div.container>div.content
   [:h1 "Survey"]
   
   [:p "Name"]
   [:input {:type :text}]
   [:br][:br]

   [:p "Few words about yourself"]

   (let [new-t @new-text]
     [:input
      {:type :text
       :placeholder "about your self..."
       :value new-t
       :on-change  #(reset! text (-> % .-target .-value))}])])
