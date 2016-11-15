(ns <<project-ns>>.views.layout
  (:require [clojure.string :as cs]
            [clojure.pprint :as pp]
            [ring.util.http-response :refer [content-type ok]]
            [ring.util.anti-forgery :refer [anti-forgery-field]]
            [ring.middleware.anti-forgery :refer [*anti-forgery-token*]]
            [hiccup.core :as hiccup]
            [hiccup.page :refer [include-js include-css html5]]
            [hiccup.element :refer [link-to]]))

<% if auth-middleware-required %>(declare ^:dynamic *identity*)<% endif %>
(declare ^:dynamic *app-context*)

(defn include-metas [& metas]
  (doall (for [[k v] metas]
           [:meta {(if (re-find #"^(fb|og):" k) :property :name) k
                   :content v}])))

;; Default metas, styles and scripts. Note that they are wrapped to functions
;; because they can be language-dependent.

(defn default-metas []
  [])

(defn default-styles []
  [])

(defn default-scripts []
  [])

(defn nav-entries []
  [{:k :home :text "Home" :path "/"}
   {:k :about :text "About" :path "/about"}])

(defn header-layout [{:keys [current-nav] :as params}]
  [:nav.navbar.navbar-default
   [:div.container.container-fluid
    [:div.navbar-header
     [:button.navbar-toggle.collapsed
      {:data-toggle "collapse" :data-target "#navbar-collapse"
       :type "button" :aria-expanded "false"}
      [:span.sr-only "Toggle navigation"]
      (doall (repeat 3 [:span.icon-bar]))]]
    [:div#navbar-collapse.collapse.navbar-collapse
     [:ul.navbar-nav.navbar-right
      (for [{:keys [k text]} (nav-entries)]
        [:li {:class (when (= k current-nav) "active")}
         text])]]]])

(defn footer-layout [params]
  [:footer.footer
   [:small "<<name>>, 2016."]])

(defn base-layout
  "Renders the site's base template. Hardcode your layout's Hiccup in the body
  of this function."
  [{:keys [title title! styles styles! scripts scripts!
           metas metas! content] :as params}]
  (let [site-title "<<name>>"
        page-title (or title!
                       (if (some-> title cs/trim seq)
                         (str title " | " site-title)
                         site-title))
        metas (or metas! (into (default-metas) metas))
        styles (or styles! (into (default-styles) styles))
        scripts (or scripts! (into (default-scripts) scripts))]
    ;; TODO: how to pass session? (flash, etc.)
    (html5
      [:html
       [:head
        [:meta {:http-equiv "Content-Type"
                :content "text.html; charset=UTF-8"}]
        [:meta {:name "viewport"
                :content "width=device-width, initial-scale=1"}]
        [:title page-title]
        (apply include-metas metas) 
        (apply include-css styles)]
       [:body
        [:div.keep-footer-down
         (header-layout params)
         ;; TODO: render flashes here (if needed)
         ;; Don't forget to use ring.middleware.flash/wrap-flash
         [:div.content content]
         [:div.push-footer]]
        [:div.footer.container (footer-layout params)]
        (apply include-js scripts)]])))

(defn error-page
  "error-details should be a map containing the following keys:
   :status - error status
   :title - error title (optional)
   :message - detailed error message (optional)

   returns a response map with the error page as the body
   and the status specified by the status key"
  [error-details]
  {:status  (:status error-details)
   :headers {"Content-Type" "text/html; charset=utf-8"}
   :body    (html5 [:pre (with-out-str (pp/pprint error-details))])})
