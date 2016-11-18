(ns <<project-ns>>.views.static-pages
  (:require [<<project-ns>>.views.layout :as layout]<% if i18n %>
            [<<project-ns>>.i18n :refer [t]]
            [taoensso.tower :refer [with-tscope]]<% endif %>
            [<<project-ns>>.helpers :refer [md->html]]
            [hiccup.core :refer [html]]
            [clojure.java.io :as io]))

(defn home-page []
  (layout/base-layout <% if i18n %>
    (with-tscope :home-page
      {:content
       [:div.container
        [:h1 (t :welcome)
         <% if cljs %> (t :wait-cljs)
         <% else %>(-> "docs/docs.md" io/resource slurp md->html)
         <% endif %>]]})
    <% else %>
    {:content
     [:div.container
      [:h1 "Welcome!"]
      <% if cljs %>"Please wait for the Clojurescript to load."
      <% else %>(-> "docs/docs.md" io/resource slurp md->html)
      <% endif %>]}<% endif %>))

(defn about-page []
  (layout/base-layout <% if i18n %>
    (with-tscope :about-page
      {:title (t :heading)
       :content [:div.container [:p (t :text)]]}) <% else %>
    {:title "About"
     :content
     (html
       [:div.container
        [:p "This is the story of <<name>>... work in progress"]])}<% endif %>))
