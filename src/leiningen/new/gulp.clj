(ns leiningen.new.gulp
  (:require [leiningen.new.common :refer :all]))

(def gulp-assets
  [["package.json" "gulp/package.json"]
   ["Gulpfile.js" "gulp/Gulpfile.js"]
   ["Gulpfile.coffee" "gulp/Gulpfile.coffee"]
   ["resources/stylus/styles.styl" "gulp/styles.styl"]
   ["resources/coffee/main.coffee" "gulp/main.coffee"]])

;; TODO: see if gulp can be launched in background in dev environment
;; TODO: add some docs? (for whom?)

(defn gulp-features [[assets options :as state]]
  (if (some #{"+gulp"} (:features options))
    [(into assets gulp-assets)
     (-> options
         #_(assoc :gulp-docs ((:selmer-renderer options)
                              (slurp-resource "gulp/docs/gulp_instructions.md")
                              options)))]
    state))
