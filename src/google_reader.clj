(ns google-reader
  (:require [clojure.string :as str]
            [clojure.xml :as xml]
            [clojure.zip :as zip]
            [clojure.data.json :as dj]
            [clojure.data.xml :as dx]
            [clj-http.client :as http]
            [google-reader.options :as opts]))

(defn- reader-url
  [path]
  (str "https://www.google.com/reader" path))

(defn auth-token
  [mailaddress password]
  (let [r (http/post "https://www.google.com/accounts/ClientLogin"
                     {:form-params {:service "reader"
                                    :Email mailaddress
                                    :Passwd password}})
        b (:body r)
        t (str/split b #"\n")]
    (reduce (fn [m s]
              (let [[_ k v] (re-matches #"^([^=]*)=(.*)$" s)]
                (assoc m (keyword (str/lower-case k)) v)))
            {}
            t)))

(defn client
  [mailaddress password]
  (auth-token mailaddress password))

(defn parse-xml-str
  [xml-str]
  (->
    xml-str
    (java.io.StringReader.)
    (org.xml.sax.InputSource.)
    (xml/parse)))

(defn call
  ([client output method url] (call client output method url nil))
  ([client output method url request]
   (let [a (if (nil? (:auth client))
             {}
             {:headers {"Authorization"
                        (str "GoogleLogin auth=" (:auth client))}})
         o (case output
             :plane identity
             :xml parse-xml-str
             :json dj/read-str)
         m (case method :get http/get :post http/post)
         u (reader-url url)
         r (merge a (case method
                      :get {:query-params request}
                      :post {:form-params request}))]
     (o (:body (m u r))))))

;; /atom/feed/

; GET  /atom/feed/:feed
(defn feed
  [client url] ; TODO n(count) client(client) r(order) ot(start_time) ck(timestamp) xt(exclude_target) c(continuation)
  (call client :xml :get (str "/atom/feed/" url)))

; GET  /atom/user/-/label/:label
(defn label
  [client label] ; TODO ...
  (call client :xml :get (str "/atom/user/-/label/" label)))

; GET  /atom/user/-/state/com.google/:state
(defn state
  [client state & options]
  (let [o (opts/parse
            {:n 20 :client nil :r "d" :ot nil :ck nil :xt nil :c nil}
            options)]
    (call client :xml :get (str "/atom/user/-/state/com.google/" state) o)))

