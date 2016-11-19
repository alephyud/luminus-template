(ns tailoredtest.test.handler
  (:require [clojure.test :refer :all]
            [ring.mock.request :refer :all]
            [tailoredtest.handler :refer :all]
            [clojure.string :as cs]
            [net.cgrand.enlive-html :refer [html-snippet select text texts]]))

(defn get-path [path]
  (-> ((app) (request :get path))
      (update :body html-snippet)))

(defn select-text [html selector]
  (-> html (select selector) texts cs/join cs/trim))

(deftest test-app
  (testing "main route"
    (let [{:keys [status body]} (get-path "/")]
      (is (= 200 status))
      (is (= "Welcome!" (select-text body [:h1])))))
  <% if cljs %><% else %>
  (testing "about route"
    (let [{:keys [status body]} (get-path "/about")]
      (is (= 200 status))
      (is (re-find #"About" (select-text body [:title])))))
  <% endif %>
  (testing "not-found route"
    (let [{:keys [status body]} (get-path "/invalid")]
      (is (= 404 status))
      (is (re-find #"not found" (select-text body [:h1]))))))  
