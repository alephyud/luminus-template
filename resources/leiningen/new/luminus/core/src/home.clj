(ns <<project-ns>>.routes.home
  (:require [<<project-ns>>.views.static-pages :refer [home-page about-page]]<% if i18n %>
            [<<project-ns>>.i18n :refer [languages root-language]]
            [<<project-ns>>.middleware :refer [wrap-language]]<% endif %>
            [<<project-ns>>.helpers :refer [md->html]]
            [compojure.core :refer [defroutes routes context GET]]
            [ring.util.http-response :as response]
            [clojure.java.io :as io]))

(defroutes site-routes
  (GET "/" [] (home-page))
  (GET "/about" [] (about-page))) 

(defroutes api-routes
  (GET "/docs" []
       (-> "docs/docs.md" io/resource slurp md->html response/ok)
       (response/header "Content-Type" "text/plain; charset=utf-8")))
<% if i18n %>
(def home-routes
  (apply routes
         (when root-language
           (wrap-language site-routes root-language))
         (for [lang languages]
           (context (str "/" (name lang)) []
             (-> (routes
                   site-routes
                   #_(GET "*" _ (page-404))) ;; i18n'd 404 page
                 (wrap-language lang))))))
<% else %>
(def home-routes (routes site-routes api-routes))
<% endif %>
