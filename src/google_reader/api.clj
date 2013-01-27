(ns google-reader.api
  (:require [clojure.data.json :as json]
            [google-reader.options :as opts]
            [google-reader :as gr]))

(def ^:dynamic *token* nil)

(defn token
  []
  (gr/call :text :get "/api/0/token"))

(defmacro with-token
  [& body]
  `(binding [*token* (token)]
     ~@body))

;; /api/0/ ( list api )

(defn tag-list
  [& options]
  (let [o (opts/parse {:timestamp nil :client nil} options)]
    (gr/call :json
             :get
             "/api/0/tag/list"
             {:query-params {:output "json"}})))

(defn subscription-list
  [& options]
  (let [o (opts/parse {:timestamp nil :client nil} options)]
    (gr/call :json
             :get
             "/api/0/subscription/list"
             {:query-params {:output "json"}})))

(defn preference-list
  [& options]
  (let [o (opts/parse {:timestamp nil :client nil} options)]
    (gr/call :json
             :get
             "/api/0/preference/list"
             {:query-params (merge {:output "json"} o)})))

(defn unread-count
  [& options]
  (let [o (opts/parse {:all nil :timestamp nil :client nil} options)]
    (gr/call :json
             :get
             "/api/0/unread-count"
             {:query-params (merge {:output "json"} o)})))

; /api/0/ ( edit api )
; need *token*
;
;(defn subscription-edit
;  [] ; TODO [& {:keys [s(feed) t(title) a(add) r(remove) ac(action)]}]
;  (gr/call :get "/api/0/subscription/edit"
;        {:query-params {:token *token*
;                        :ac "edit"
;                        :a }}))

(defn tag-edit
  [& options]
  (let [o (opts/parse {:feed nil :public nil} options)]
    (gr/call :text
             :post
             "/api/0/tag/edit"
             {:form-params {:s feed
                            :pub public
                            :T *token*}}))

