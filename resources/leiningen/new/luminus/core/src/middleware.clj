(ns <<project-ns>>.middleware
  (:require [<<project-ns>>.env :refer [defaults]]<% if not service %>
            [clojure.string :as cs]
            [clojure.tools.logging :as log]
            [clj-time.core]
            [ring.util.codec :refer [url-decode]]
            [<<project-ns>>.views.layout :refer [*app-context* error-page]]
            [ring.middleware.anti-forgery :refer [wrap-anti-forgery]]
            [ring.middleware.format :refer [wrap-restful-format]]<% endif %>
            [<<project-ns>>.config :refer [env]]<% if immutant-session %>
            [ring.middleware.flash :refer [wrap-flash]]
            [immutant.web.middleware :refer [wrap-session]]<% else %>
            [ring-ttl-session.core :refer [ttl-memory-store]]<% endif %>
            [ring.middleware.defaults :refer [site-defaults wrap-defaults]]<% if auth-middleware-required %>
            <<auth-middleware-required>><% if auth-session %>
            <<auth-session>><% endif %><% if auth-jwe %>
            <<auth-jwe>><% endif %><% endif %><% if i18n %>
            [taoensso.tower :refer [with-tscope]]
            [<<project-ns>>.i18n :refer [t with-language default-language]]<% endif %>)<% if not service %>
  (:import [javax.servlet ServletContext])<% endif %>)
<% if auth-middleware-required %>(declare ^:dynamic *identity*)<% endif %>
(declare ^:dynamic *app-context*)
<% if not service %>
(defn wrap-context [handler]
  (fn [request]
    (binding [*app-context*
              (if-let [context (:servlet-context request)]
                ;; If we're not inside a servlet environment
                ;; (for example when using mock requests), then
                ;; .getContextPath might not exist
                (try (.getContextPath ^ServletContext context)
                     (catch IllegalArgumentException _ context))
                ;; if the context is not specified in the request
                ;; we check if one has been specified in the environment
                ;; instead
                (:app-context env))]
      (handler request))))

(defn wrap-internal-error [handler error-page]
  (fn [req]
    (try
      (handler req)
      (catch Throwable th
        (log/error th)<% if i18n %>
        (with-tscope :error-page
          (error-page {:status 500
                       :title (t :500-title)
                       :message (t :500-text)}))<% else %>
        (error-page {:status 500
                     :title "Something very bad has happened!"
                     :message "We've dispatched a team of highly trained gnomes to take care of the problem."})<% endif %>))))

(defn wrap-simple-logger [handler]
  (fn [req]
    (let [start-time-pretty (clj-time.core/now)
          start-time (System/nanoTime)
          res (handler req)
          end-time (System/nanoTime)
          log-rec [start-time-pretty
                   (or (get-in req [:headers "x-forwarded-for"])
                       (:remote-addr req))
                   (cs/upper-case (name (:request-method req)))
                   (:uri req)
                   (some-> (:query-string req)
                           ;; Prettify and add LTR mark for proper look.
                           url-decode (str \u200E))
                   (format " %d (%.1f ms)"
                           (:status res)
                           (/ (double (- end-time start-time)) 1e6))]
          log-str (cs/join " " log-rec)]
      (case {:status res}
        404 (log/warn log-str)
        403 (log/warn log-str)
        500 (log/error log-str)
        (log/info log-str))
      res)))

(defn wrap-csrf [handler error-page]
  (wrap-anti-forgery
    handler
    {:error-response
     (error-page <% if i18n %>
       (with-tscope :error-page
         {:status 403
          :title (t :403-csrf-title)
          :content (t :403-csrf-text)}) <% else %>
       {:status 403
        :title "Invalid anti-forgery token"
        :content "Your page could not be loaded. Please try to reload this page and see if the problem persists."}<% endif %>)}))

(defn wrap-formats [handler]
  (let [wrapped (wrap-restful-format
                  handler
                  {:formats [:json-kw :transit-json :transit-msgpack]})]
    (fn [request]
      ;; disable wrap-formats for websockets
      ;; since they're not compatible with this middleware
      ((if (:websocket? request) handler wrapped) request))))
<% endif %><% if auth-middleware-required %><% if not service %>
(defn on-error [request response]
  (error-page
    {:status 403
     :title (str "Access to " (:uri request) " is not authorized")}))
<% else %>
(defn on-error [request response]
  {:status 403
   :headers {}
   :body (str "Access to " (:uri request) " is not authorized")})
<% endif %>
(defn wrap-restricted [handler]
  (restrict handler {:handler authenticated?
                     :on-error on-error}))<% if auth-jwe %>

(def secret (random-bytes 32))

(def token-backend
  (jwe-backend {:secret secret
                :options {:alg :a256kw
                          :enc :a128gcm}}))

(defn token [username]
  (let [claims {:user (keyword username)
                :exp (plus (now) (minutes 60))}]
    (encrypt claims secret {:alg :a256kw :enc :a128gcm})))
<% endif %><% if auth-session %>

(defn wrap-identity [handler]
  (fn [request]
    (binding [*identity* (get-in request [:session :identity])]
      (handler request))))
<% endif %>
(defn wrap-auth [handler]
  (let [backend <% if auth-jwe %>token-backend<% endif %><% if auth-session %>(session-backend)<% endif %>]
    (-> handler<% if auth-session %>
        wrap-identity<% endif %>
        (wrap-authentication backend)
        (wrap-authorization backend))))
<% endif %><% if i18n %>
(defn wrap-language [handler lang]
  (fn [request]
    (with-language lang (handler request))))

(defn wrap-default-language [handler]
  (fn [request]
    (with-language default-language (handler request))))
<% endif %>
(defn wrap-base [handler {:keys [error-page]}]
  (-> ((:middleware defaults) handler)
      wrap-simple-logger<% if auth-middleware-required %>
      wrap-auth<% endif %><% if immutant-session %>
      wrap-flash
      (wrap-session {:cookie-attrs {:http-only true}})
      (wrap-defaults
        (-> site-defaults
            (assoc-in [:security :anti-forgery] false)
            (dissoc :session)))<% else %>
      (wrap-defaults
        (-> site-defaults
            (assoc-in [:security :anti-forgery] false)
            (assoc-in  [:session :store] (ttl-memory-store (* 60 30)))))<% endif %><% if not service %>
      wrap-context
      (wrap-internal-error error-page)<% endif %>))
