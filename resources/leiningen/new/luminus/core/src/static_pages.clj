(ns <<project-ns>>.views.static-pages
  (:require [<<project-ns>>.views.layout :as layout]
            [hiccup.core :refer [html]]
            [clojure.java.io :as io])  
  (:import [com.github.rjeschke.txtmark Processor]))

(defn home-page []
  (layout/base-layout
    {:content
     <% if cljs %>
     "Please wait for the Clojurescript to load."
     <% else %>
     (-> "docs/docs.md" io/resource slurp Processor/process)
     <% endif %>
     }))

(defn about-page []
  (layout/base-layout
    {:title "About"
     :content (html
                [:p "This is the story of <<name>>... work in progress"])}))
