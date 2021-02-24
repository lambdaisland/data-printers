(ns lambdaisland.my-data-reader)

(defn read-my-type [m]
  `(lambdaisland.data-printers-test/->MyType (:x ~m)))
