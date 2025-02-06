(ns lambdaisland.data-printers.auto
  "Convenience ns for one-stop-shop printer registration, clj-only

  Will register printers for any libraries `lambdaisland.data-printers` knows
  about and is able to find at load time."
  (:require
   [lambdaisland.data-printers :as dp]))

(defn- resolve-var [s]
  (try
    @(requiring-resolve s)
    (catch Exception e)))

(def register-deep-diff (resolve-var 'lambdaisland.data-printers.deep-diff/register-deep-diff))
(def register-deep-diff2 (resolve-var 'lambdaisland.data-printers.deep-diff2/register-deep-diff2))
(def register-write-handler (resolve-var 'lambdaisland.data-printers.transit/register-write-handler))
(def register-puget (resolve-var 'lambdaisland.data-printers.puget/register-puget))
(def register-cider-puget (resolve-var 'lambdaisland.data-printers.cider-puget/register-cider-puget))

(defn register-printer
  "Register print handlers for `clojure.core/print-method`,
  `clojure.core/print-dup`, and `clojure.pprint/simple-dispatch`, as well as
  lambdaisland/deep-diff, lambdaisland/deep-diff2, Puget, and Transit, if any of
  those libraries are present on the classpath."
  [type tag to-edn]
  (dp/register-print type tag to-edn)
  (dp/register-pprint type tag to-edn)
  (when register-deep-diff (register-deep-diff type tag to-edn))
  (when register-deep-diff2 (register-deep-diff2 type tag to-edn))
  (when register-write-handler (register-write-handler type tag to-edn))
  (when register-puget (register-puget type tag to-edn))
  (when register-cider-puget (register-cider-puget type tag to-edn)))
