(ns lambdaisland.data-printers.deep-diff
  #?(:clj (:require [lambdaisland.data-printers :refer [type-name]]
                    [lambdaisland.deep-diff.printer :as ddiff-printer]
                    [puget.printer :as puget])))

(defn register-deep-diff [type tag to-edn]
  #?(:clj (ddiff-printer/register-print-handler!
           (symbol (type-name type))
           (puget/tagged-handler tag to-edn))))
