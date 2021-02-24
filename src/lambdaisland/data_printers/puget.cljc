(ns lambdaisland.data-printers.puget
  #?(:clj (:require [puget.printer :as puget]
                    [puget.dispatch :as dispatch])))

#?(:clj (def handlers (atom {})))

#?(:clj (defonce original-handlers puget/common-handlers))

(defn register-puget [type tag to-edn]
  #?@(:clj
      [(swap! handlers assoc type (puget/tagged-handler tag to-edn))
       (alter-var-root
        #'puget/common-handlers
        (constantly
         (dispatch/chained-lookup original-handlers (dispatch/inheritance-lookup @handlers))))]))
