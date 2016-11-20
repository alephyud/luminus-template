(ns <<project-ns>>.helpers
  (:import [com.github.rjeschke.txtmark Processor Configuration]))

(def md-config
  ;; Use extended txtmark profile, which allows for code blocks in Markdown.
  (.. (Configuration/builder) forceExtentedProfile build))

(defn md->html-text
  "Renders markdown as HTML. Conforms to txtmark's extended mode.
  Note that a single paragraph will be wrapped in a <p> tag."
  [md-input]
  (Processor/process md-input md-config))

(defn- remove-wrapping-p-tag
  "If the text begins with a <p>, ends with a </p> and contains no <p>s
  inside, removes the surrounding <p>/</p> tags."
  [s]
  (or (when-let [[_ content] (re-matches #"<p>(.*)</p>\s*"s)]
        (when-not (re-find #"<p>" content)
          content))
      s))

(defn md->html
  "Renders markdown as HTML. Conforms to txtmark's extended mode.
  Does not wrap a single paragraph in a <p> tag."
  [md-input]
  (remove-wrapping-p-tag (md->html-text md-input)))
