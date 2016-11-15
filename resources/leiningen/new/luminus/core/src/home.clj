(ns <<project-ns>>.routes.home
  (:require [<<project-ns>>.views.static-pages :refer [home-page about-page]]
            [compojure.core :refer [defroutes GET]]
            [ring.util.http-response :as response]
            [clojure.java.io :as io]) 
  (:import [com.github.rjeschke.txtmark Processor]))

(defroutes home-routes
  (GET "/" [] (home-page))
  (GET "/about" [] (about-page)) 
  (GET "/docs" []
       (-> "docs/docs.md" io/resource slurp Processor/process response/ok)
       (response/header "Content-Type" "text/plain; charset=utf-8")))
