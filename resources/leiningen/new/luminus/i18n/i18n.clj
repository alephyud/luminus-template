(ns <<project-ns>>.i18n
  (:require [taoensso.tower :as tower]
            [medley.core :as medley]
            [yaml.core :as yaml]
            [<<project-ns>>.helpers :refer [md->html]]
            [clojure.java.io :as io]
            [clojure.string :as cs]))

(def default-language :en)
(def root-language :en)
(def ^:dynamic *page-language* default-language)

(def language-locales {:en "en_GB"
                       :ru "ru_RU"})

(def languages (set (keys language-locales)))

(defmacro with-language [lang & body]
  `(binding [*page-language*
             (get languages
                  (cond-> ~lang string? keyword)
                  :en)]
     ~@body))

(defn- get-lang [lang coll]
  (let [leaf? (comp not map? second first)]
    (if (leaf? coll)
      (some-> (lang coll) md->html)
      (into {} (medley/map-vals #(get-lang lang %) coll)))))

(def unbound-t
  ((fn []
     (tower/make-t
       {:dictionary (let [i18n-text (-> "content/i18n.yml" io/resource slurp
                                        (yaml/parse-string true))]
                      (->> languages
                           (map #(-> [% (get-lang % i18n-text)]))
                           (into {})))
        :dev-mode?  true}))))

(defn t [& args]
  (apply unbound-t *page-language* args))

(defn t-sel
  "Generates a list of keys (from val-list) and the respective i18n'd strings
  in the given tscope - useful for populating a select element."
  [tscope val-list]
  (map #(-> [% (t (keyword (str (name tscope) "/" (name %))))]) val-list))

(defn local-url
  "Makes a local link relative to /. In the future, this function may be
  also used to check for consistency of local links / absence of 404s."
  [url]
  (let [lang *page-language*
        url (or url "/")
        url (if (not= \/ (first url)) (str \/ url) url)
        lang-prefix (if (= lang root-language) "" (str "/" (name lang)))]
    (str lang-prefix url)))

