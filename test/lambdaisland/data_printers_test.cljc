(ns lambdaisland.data-printers-test
  (:require #?@(:clj [[puget.printer :as puget]
                      [lambdaisland.deep-diff.printer :as ddiff-printer]])
            [clojure.pprint :as pprint]
            [clojure.test :refer [deftest testing is]]
            [cognitect.transit :as transit]
            [lambdaisland.data-printers :as dp]
            [lambdaisland.data-printers.deep-diff :as dp-ddiff]
            [lambdaisland.data-printers.deep-diff2 :as dp-ddiff2]
            [lambdaisland.data-printers.transit :as dp-transit]
            [lambdaisland.data-printers.puget :as dp-puget]
            [lambdaisland.deep-diff2.printer-impl :as ddiff2-printer-impl])
  #?(:clj (:import (java.io ByteArrayInputStream ByteArrayOutputStream))))

(deftype MyType [x]
  ;; Value semantics
  #?@(:clj
      [Object
       (equals [this that]
               (and (instance? MyType that)
                    (= (.-x this) (.-x that))))]
      :cljs
      [cljs.core/IEquiv
       (-equiv [this that]
               (and (instance? MyType that)
                    (= (.-x this) (.-x that))))]))

(defn to-edn [obj]
  {:x (.-x obj)})

(defn read-my-type [m]
  `(->MyType (:x ~m)))

(dp/register-print MyType 'my/type to-edn)
(dp/register-pprint MyType 'my/type to-edn)
(dp-puget/register-puget MyType 'my/type to-edn)
(dp-ddiff/register-deep-diff MyType 'my/type to-edn)
(dp-ddiff2/register-deep-diff2 MyType 'my/type to-edn)
(dp-transit/register-write-handler MyType 'my/type to-edn)

(def obj (->MyType 1))

(deftest regular-print
  (is (= "#my/type {:x 1}"
         (pr-str obj)))

  (is (= #?(:clj "#my/type #=(clojure.lang.PersistentArrayMap/create {:x 1})"
            :cljs "#my/type {:x 1}")
         (binding [*print-dup* true]
           (pr-str obj)))))

(deftest pretty-print
  (is (= "#my/type {:x 1}\n"
         (with-out-str
           (pprint/pprint obj)))))

#?(:clj
   (deftest puget-test
     (is (= "#my/type {:x 1}\n"
            (with-out-str
              (puget/pprint obj))))))

#?(:clj
   (deftest deep-diff-test
     (is (= "\u001B[35m#my/type\u001B[0m \u001B[1;31m{\u001B[0m\u001B[1;33m:x\u001B[0m \u001B[36m1\u001B[0m\u001B[1;31m}\u001B[0m\n"
            (with-out-str
              (let [printer (ddiff-printer/puget-printer)]
                (-> obj
                    (ddiff-printer/format-doc printer)
                    (ddiff-printer/print-doc printer))))))))

(deftest deep-diff2-test
  (is (= "\u001B[35m#my/type\u001B[0m \u001B[1;31m{\u001B[0m\u001B[1;33m:x\u001B[0m \u001B[36m1\u001B[0m\u001B[1;31m}\u001B[0m\n"
         (with-out-str
           (let [printer (ddiff2-printer-impl/puget-printer)]
             (-> obj
                 (ddiff2-printer-impl/format-doc printer)
                 (ddiff2-printer-impl/print-doc printer)))))))

(deftest transit-test
  (let [encoded "[\"~#my/type\",[\"^ \",\"~:x\",1]]"]
    (is (= encoded
           #?(:clj
              (let [baos (ByteArrayOutputStream.)
                    writer (transit/writer baos :json {:handlers @dp-transit/write-handlers})]
                (transit/write writer obj)
                (.toString baos "utf-8"))
              :cljs
              (transit/write (transit/writer :json {:handlers @dp-transit/write-handlers}) obj))))

    (is (= obj
           #?(:clj (let [bais (ByteArrayInputStream. (.getBytes encoded))
                         reader (transit/reader bais :json {:handlers (dp-transit/data-reader-handlers)})]
                     (transit/read reader))
              :cljs (transit/read (transit/reader :json {:handlers (dp-transit/data-reader-handlers)}) encoded))))))
