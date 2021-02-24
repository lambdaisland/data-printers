(ns lambdaisland.data-printers.deep-diff2
  (:require [lambdaisland.data-printers :refer [type-name]]
            [lambdaisland.deep-diff2.printer-impl :as ddiff2-printer]
            [lambdaisland.deep-diff2.puget.printer :as puget2-printer]))

(defn register-deep-diff2 [type tag to-edn]
  (ddiff2-printer/register-print-handler!
   (symbol (type-name type))
   (puget2-printer/tagged-handler tag to-edn)))
