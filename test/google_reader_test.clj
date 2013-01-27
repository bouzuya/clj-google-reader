(ns google-reader-test
  (:import [org.joda.time DateTime DateTimeZone LocalDate])
  (:use clojure.test))

(defn starred-from
  [client timestamp]
  (->
    client
    (gr/state "starred" :r "o" :ot (quot timestamp 1000)) ; :ot is seconds. ms -> s.
    zip/xml-zip
    (dzx/xml->
      :entry
      (fn [loc]
        {:title (dzx/xml1-> loc :title dzx/text)
         :link (dzx/xml1-> loc :link (dzx/attr :href))
         :timestamp (Long/parseLong (dzx/xml1-> loc (dzx/attr :gr:crawl-timestamp-msec)))}))))

(defn starred
  [today]
  (let [client (gr/client (System/getenv "GOOGLE_USERNAME")
                          (System/getenv "GOOGLE_PASSWORD"))]
    (loop [result nil
           ts (->
                today
                (.toDateTimeAtStartOfDay (DateTimeZone/getDefault))
                (.toInstant)
                (.getMillis))]
      (let [[o n] (split-with
                    (fn [e]
                      (.contains (.toInterval today) (:timestamp e)))
                    (-> client (starred-from ts)))
            r (concat o result)]
        (if (or n (empty? o))
          r
          (recur r (inc (:timestamp (last o)))))))))

(defn -main
  [& args]
  (clojure.pprint/pprint
    (map (fn [e] {:timestamp (DateTime. (:timestamp e) (DateTimeZone/getDefault))
                  :title (:title e)})
         (starred (LocalDate/now)))))

