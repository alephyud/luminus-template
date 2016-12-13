(ns <<project-ns>>.views.layout
  (:require [clojure.string :as cs]
            [clojure.pprint :as pp]<% if i18n %>
            [<<project-ns>>.i18n :refer [t *page-language* local-url]]
            [taoensso.tower :refer [with-tscope]]<% endif %>
            [ring.util.http-response :refer [content-type ok]]
            [ring.util.anti-forgery :refer [anti-forgery-field]]
            [ring.middleware.anti-forgery :refer [*anti-forgery-token*]]
            [hiccup.core :as hiccup]
            [hiccup.page :refer [include-js include-css html5]]
            [hiccup.element :refer [link-to]]))

(defn include-metas [& metas]
  (doall (for [[k v] metas]
           [:meta {(if (re-find #"^(fb|og):" k) :property :name) k
                   :content v}])))

;; Default metas, styles and scripts. Note that they are wrapped in functions
;; because they can be language-dependent.

(defn default-metas []
  ;; TODO: i18n
  (let [root-url "http://<<project-ns>>.com"]
    ["title" "<<name>>"
     "description" "An awesome new project!"
     "keywords" "<<name>>, awesome"
     "author" "me"
     "og:type" "website"
     "og:title" "<<name>> - a new cool app!"
     "og:description" "Take a look at this!"
     "og:url" index-url
     "og:image" (str index-url "/title.jpg")
     "twitter:card" "summary"
     "twitter:url" index-url
     "twitter:description" "Take a look at this!"
     "twitter:image" (str index-url "/title.jpg")]))

(def statics-revision "?revision=1")

(defn default-styles []
  [(str "/css/styles.css" statics-revision)])

(defn default-scripts []
  [(str "/js/scripts.js" statics-revision)<% if cljs %>
   (str "/js/app.js" statics-revision)<% endif %>])
<% if i18n %>
(defn nav-entries []
  (with-tscope :nav-menu
  [{:k :home :text (t :home) :path "/"}
   {:k :about :text (t :about) :path "/about"}]))
<% else %>
(defn nav-entries []
  [{:k :home :text "Home" :path "/"}
   {:k :about :text "About" :path "/about"}])
<% endif %>
(defn header-layout [{:keys [current-nav] :as params}]
  [:nav.navbar.navbar-default
   [:div.container.container-fluid
    [:div.navbar-header
     [:button.navbar-toggle.collapsed
      {:data-toggle "collapse" :data-target "#navbar-collapse"
       :type "button" :aria-expanded "false"}
      [:span.sr-only "Toggle navigation"]
      (doall (repeat 3 [:span.icon-bar]))]
     (link-to {:class :navbar-brand} <% if i18n %>(local-url "/")<% else %>"/"<% endif %> "<<name>>")]
    [:div#navbar-collapse.collapse.navbar-collapse
     [:ul.nav.navbar-nav.navbar-right
      (for [{:keys [k text path]} (nav-entries)]
        [:li {:class (when (= k current-nav) "active")}
         (link-to <% if i18n %>(local-url path)<% else %>path<% endif %> text)])]]]])

(defn footer-layout [params]
  [:div.container
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
    (html5 <% if i18n %>{:lang *page-language*}<% endif %>
      [:html
       [:head
        [:meta {:http-equiv "Content-Type"
                :content "text/html; charset=UTF-8"}]
        [:meta {:name "viewport"
                :content "width=device-width, initial-scale=1"}]
        [:meta {:http-equiv "X-UA-Compatible" :content "IE=edge"}]
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
        [:footer.footer (footer-layout params)]
        (apply include-js scripts)]])))

(defn- error-body
  [{:keys [title message] :as error-details}]
  [:div.container
   [:h1 (or title "An error has occurred")]
   [:div message]])

(defn error-page
  "The argument should be a map containing the following keys:
   :status - error status
   :title - error title (optional)
   :message - detailed error message (optional)

   returns a response map with the error page as the body
   and the status specified by the status key"
  [{:keys [status title] :as error-details}]
  {:headers {"Content-Type" "text/html; charset=utf-8"}
   :status  status
   :body    (html5 <% if i18n %>{:lang *page-language*}<% endif %>
               (base-layout {:title title
                             :content (error-body error-details)}))})
<% if i18n %>
(defn error-404 []
  (with-tscope :error-page
    (error-page
      {:status 404
       :title (t :404-title)
       :message (format (t :404-text) (local-url "/"))})))
<% else %>
(defn error-404 []
  (error-page
    {:status 404
     :title "Page not found"
     :message (html
                [:p "The page you were looking for was not found."]
                [:p (link-to "/" "Go to the home page")])}))
<% endif %>
