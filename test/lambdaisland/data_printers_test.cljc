(ns lambdaisland.data-printers-test
  (:require #?@(:clj [[puget.printer :as puget]
                      [lambdaisland.deep-diff.printer :as ddiff-printer]])
            [clojure.pprint :as pprint]
            [clojure.test :refer [deftest testing is]]
            [lambdaisland.data-printers :as dp]
            [lambdaisland.data-printers.deep-diff :as dp-ddiff]
            [lambdaisland.data-printers.deep-diff2 :as dp-ddiff2]
            [lambdaisland.data-printers.puget :as dp-puget]
            [lambdaisland.deep-diff2.printer-impl :as ddiff2-printer-impl]))

(deftype MyType [x])

(defn to-edn [obj]
  {:x (.-x obj)})

(dp/register-print MyType 'my/type to-edn)
(dp/register-pprint MyType 'my/type to-edn)
(dp-puget/register-puget MyType 'my/type to-edn)
(dp-ddiff/register-deep-diff MyType 'my/type to-edn)
(dp-ddiff2/register-deep-diff2 MyType 'my/type to-edn)

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
