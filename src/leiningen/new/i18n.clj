(ns leiningen.new.i18n
  (:require [leiningen.new.common :refer :all]))

(def i18n-assets
  [["src/clj/{{sanitized}}/i18n.clj" "i18n/i18n.clj"]
   ["resources/content/i18n.yml" "i18n/i18n.yml"]])

(def i18n-dependencies
  [['com.taoensso/tower "3.1.0-beta4"]
   ['io.forward/yaml "1.0.3"]])

(defn i18n-features [[assets options :as state]]
  (if (some #{"+i18n"} (:features options))
    [(into assets i18n-assets)
     (-> options
         (append-options :dependencies i18n-dependencies)
         (assoc
           :i18n true
           :i18n-docs ((:selmer-renderer options)
                       (slurp-resource "i18n/docs/i18n_instructions.md")
                       options)))]
    state))
