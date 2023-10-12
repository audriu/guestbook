(ns shop.common 
  (:require [ajax.core :as ajax]
            [reagent.core :as reagent]
            [reagent.session :as session]
            [shop.validation :refer [validate-user]]))
    
(defn modal [header body footer]
  [:div
   [:div.modal-dialog
    [:div.modal-content
     [:div.modal-header [:h3 header]]
     [:div.modal-body body]
     [:div.modal-footer
      [:div.bootstrap-dialog-footer
       footer]]]]
   [:div.modal-backdrop.fade.in]])

(defn input [type id placeholder fields]
  [:input.form-control.input-lg
   {:type
    type
    :placeholder placeholder
    :value
    (id @fields)
    :on-change
    #(swap! fields assoc id (-> % .-target .-value))}])

(defn form-input [type label id placeholder fields optional?]
  [:div.form-group
   [:label label]
   (if optional?
     [input type id placeholder fields]
     [:div.input-group
      [input type id placeholder fields]
      [:span.input-group-addon
       " ✱ "]])])

(defn text-input [label id placeholder fields & [optional?]]
  (form-input :text label id placeholder fields optional?))

(defn password-input [label id placeholder fields & [optional?]]
  (form-input :password label id placeholder fields optional?))

(defn register! [fields errors]
  (reset! errors (validate-user @fields))
  (when-not @errors
    ;;TODO pass via params - not via url params
    (ajax/POST "/api/service-routes/register"
               {:url-params @fields
                :handler
                #(do
                   (session/put! :identity (:id @fields))
                   (reset! fields {})
                   (session/remove! :modal))
                :error-handler
                #(do
                   (reset! errors {:server-error (get-in % [:response])}))})))

(defn registration-form []
  (let [fields (reagent/atom {})
        form-errors (reagent.ratom/reaction (validate-user @fields))
        error (reagent/atom nil)]
    (fn []
      [modal
       [:div "Registration"]
       [:div
        [:div.well.well-sm
         [:strong " ✱ required field"]]
        [text-input "name" :id "enter a user name" fields]
        (when-let [error (:id @form-errors)]
          [:div.alert.alert-danger error])

        [text-input "first-name" :first-name "enter your first name" fields true]
        (when-let [error (:first-name @form-errors)]
          [:div.alert.alert-danger error])
        [text-input "last-name" :last-name "enter your last name" fields true]
        (when-let [error (:last-name @form-errors)]
          [:div.alert.alert-danger error])
        [text-input "email" :email "email" fields true]
        (when-let [error (:email @form-errors)]
          [:div.alert.alert-danger error])

        [password-input "password" :pass "enter a password" fields]
        (when-let [error (:pass @form-errors)]
          [:div.alert.alert-danger error])
        [password-input "password" :pass-confirm "re-enter the password" fields]
        (when-let [error (:server-error @form-errors)]
          [:div.alert.alert-danger error])]
       [:div
        (when @error
          [:div.alert.alert-danger (str @error)])
        [:button.btn.btn-primary
         {:on-click #(register! fields error)}
         "Register"]
        [:button.btn.btn-danger
         {:on-click #(session/remove! :modal)}
         "Cancel"]]])))

(defn registration-button []
  [:a.btn
   {:on-click #(session/put! :modal :register)} "register"])

(defn login! [fields error]
  (let [{:keys [id pass]} @fields]
    (reset! error nil)
    (ajax/POST "/api/service-routes/login"
               {:url-params @fields
                :handler
                #(do
                   (session/remove! :modal)
                   (session/put! :identity id)
                   (reset! fields nil))
                :error-handler #(reset! error %)})))

(defn login-form []
  (let [fields (reagent/atom {})
        error (reagent/atom nil)]
    (fn []
      [modal
       [:div "Login"]
       [:div
        [:div.well.well-sm
         [:strong " ✱ required field"]]
        [text-input "name" :id "enter a user name" fields]
        [password-input "password" :pass "enter a password" fields]
        (when-let [error @error]
          [:div.alert.alert-danger error])]
       [:div
       (when @error
          [:div.alert.alert-danger (str @error)])
        [:button.btn.btn-primary
         {:on-click #(login! fields error)}
         "Login"]
        [:button.btn.btn-danger
         {:on-click #(session/remove! :modal)}
         "Cancel"]]])))

(defn login-button []
  [:a.btn
   {:on-click #(session/put! :modal :login)}"login"])
