(ns google-reader.xml-test
  (:require [clojure.xml :as xml]
            [clojure.zip :as zip]
            [clojure.data.zip.xml :as dzx])
  (:use clojure.test))

(def xml-str-1
  (str "<root id='1'>"
       "  <parent id='2'>"
       "    <child id='3'/>"
       "  </parent>"
       "  <parent id='4'>"
       "    <child id='5'/>"
       "    <child id='6'/>"
       "  </parent>"
       "</root>"))

;; Atom Feed example. 
;; http://ja.wikipedia.org/wiki/Atom
(def xml-str-2
  (str
    "<?xml version=\"1.0\" encoding=\"utf-8\"?>"
    "<feed xmlns=\"http://www.w3.org/2005/Atom\">"
    " <title>Example Feed</title>"
    " <link href=\"http://example.org/\"/>"
    " <updated>2003-12-13T18:30:02Z</updated>"
    " <author>"
    "   <name>John Doe</name>"
    " </author>"
    " <id>urn:uuid:60a76c80-d399-11d9-b93C-0003939e0af6</id>"
    " <entry>"
    "   <title>Atom-Powered Robots Run Amok</title>"
    "   <link href=\"http://example.org/2003/12/13/atom03\"/>"
    "   <id>urn:uuid:1225c695-cfb8-4ebb-aaaa-80da344efa6a</id>"
    "   <updated>2003-12-13T18:30:02Z</updated>"
    "   <summary>Some text.</summary>"
    " </entry>"
    "</feed>"))

(def xml-str-3
  (str
    "<?xml version=\"1.0\" encoding=\"utf-8\"?>"
    "<feed xmlns=\"http://www.w3.org/2005/Atom\">"
    " <title>Example Feed</title>"
    " <link href=\"http://example.org/\"/>"
    " <updated>2003-12-13T18:30:02Z</updated>"
    " <author>"
    "   <name>John Doe</name>"
    " </author>"
    " <id>urn:uuid:60a76c80-d399-11d9-b93C-0003939e0af6</id>"
    " <entry>"
    "   <title>Atom-Powered Robots Run Amok</title>"
    "   <link href=\"http://example.org/2003/12/13/atom03\"/>"
    "   <id>urn:uuid:1225c695-cfb8-4ebb-aaaa-80da344efa6a</id>"
    "   <updated>2003-12-13T18:30:02Z</updated>"
    "   <summary>Some text.</summary>"
    " </entry>"
    " <entry>"
    "   <title>entry title 2</title>"
    "   <id>urn:uuid:aedc9760-6790-11e2-bcfd-0800200c9a66</id>"
    "   <updated>2003-12-13T18:30:02Z</updated>"
    " </entry>"
    "</feed>"))

(with-test
  (defn parse-str
    [xml]
    (->
      xml
      (java.io.StringReader.)
      (org.xml.sax.InputSource.)
      (xml/parse)))
  (is (= (parse-str xml-str-1)
         {:tag :root,
          :attrs {:id "1"},
          :content
          [{:tag :parent,
            :attrs {:id "2"},
            :content [{:tag :child, :attrs {:id "3"}, :content nil}]}
           {:tag :parent,
            :attrs {:id "4"},
            :content
            [{:tag :child, :attrs {:id "5"}, :content nil}
             {:tag :child, :attrs {:id "6"}, :content nil}]}]})))

; [clojure.zip :as zip]
(with-test
  (defn zip-xml
    [xml]
    (zip/xml-zip xml))
  (is (= (->
           (parse-str xml-str-1)
           zip-xml
           (dzx/xml-> :parent :child (dzx/attr :id)))
         ["3" "5" "6"])))

; use [clojure.data.zip.xml :as dzx]
(with-test
  (defn zip-xml-str
    [xml-str]
    (->
      xml-str
      parse-str
      zip-xml))
  (is (= (->
           (zip-xml-str xml-str-2)
           (dzx/xml-> :entry :title dzx/text)
           first)
         "Atom-Powered Robots Run Amok"))
  (is (= (->
           (zip-xml-str xml-str-2)
           (dzx/xml1-> :entry :title dzx/text))
         "Atom-Powered Robots Run Amok"))
  (is (= (->
           (zip-xml-str xml-str-2)
           (dzx/xml-> :entry :title dzx/text)
           first)
         (->
           (zip-xml-str xml-str-2)
           (dzx/xml1-> :entry :title dzx/text))))
  (is (= (->
           (zip-xml-str xml-str-3)
           (dzx/xml-> :entry :title dzx/text))
         ["Atom-Powered Robots Run Amok" "entry title 2"]))
  (is (= (->
           (zip-xml-str xml-str-3)
           (dzx/xml->
             :entry
             (fn [loc]
               {:title (dzx/xml1-> loc :title dzx/text)
                :link (dzx/xml1-> loc :link (dzx/attr :href))})))
         [{:title "Atom-Powered Robots Run Amok"
           :link "http://example.org/2003/12/13/atom03"}
          {:title "entry title 2"
           :link nil}])))

