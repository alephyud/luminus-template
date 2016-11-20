(ns <<project-ns>>.routes.home
  (:require [<<project-ns>>.views.static-pages :refer [home-page about-page]]
            [<<project-ns>>.views.layout :refer [error-404]]<% if i18n %>
            [<<project-ns>>.i18n :refer [languages root-language
                                         with-language default-language]]
            [<<project-ns>>.middleware :refer [wrap-language]]<% endif %>
            [<<project-ns>>.helpers :refer [md->html]]
            [compojure.core :refer [defroutes routes context GET]]
            [compojure.route :refer [not-found]]
            [ring.util.http-response :as response]
            [ring.util.response :refer [redirect]]
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
         (if root-language
           (wrap-language site-routes root-language)
           (GET "/" [] (redirect (str "/" (name default-language)))))
         (for [lang languages]
           (context (str "/" (name lang)) []
             (routes
               (wrap-language site-routes lang)
               ;; Internationalized 404 page
               (not-found (with-language lang (error-404))))))))
<% else %>
(def home-routes (routes site-routes api-routes))
<% endif %>
