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

(deftest app-test
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
<% if i18n %>
(deftest i18n-app-test
  (testing "ru/main route"
    (let [{:keys [status body]} (get-path "/ru")]
      (is (= 200 status))
      (is (= "Добро пожаловать!" (select-text body [:h1])))))
  <% if cljs %><% else %>
  (testing "ru/about route"
    (let [{:keys [status body]} (get-path "/ru/about")]
      (is (= 200 status))
      (is (re-find #"О сайте" (select-text body [:title])))))
  <% endif %>
  (testing "ru/not-found route"
    (let [{:keys [status body]} (get-path "/ru/invalid")]
      (is (= 404 status))
      (is (re-find #"Страница не найдена" (select-text body [:h1]))))))  
<% endif %>
