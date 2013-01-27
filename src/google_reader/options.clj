(ns google-reader.options)

(defn parse
  [defaults values]
  (->>
    (apply hash-map values)
    (merge defaults)
    (filter (comp (set (keys defaults)) key))
    (remove (comp nil? val))
    (into {})))

