(ns lambdaisland.data-printers.transit
  "Create write handlers for Transit

  The handlers simply get added to an atom, it's up to you to refer to them
  when constructing your transit writer."
  (:require [cognitect.transit :as transit]
            #?(:clj [clojure.java.io :as io]))
  #?(:cljs (:require-macros [lambdaisland.data-printers.transit])))

(def write-handlers (atom {}))

(defn register-write-handler [type tag to-edn]
  (swap! write-handlers
         assoc type
         (transit/write-handler (constantly (str tag)) to-edn)))

#?(:clj
   (defmacro data-reader-handlers
     "Turn your data_readers.cljc into transit read handlers. Note that this expects
  the data-reader functions to return forms (essentially act like macros), which
  is the proper way to provide cross platform data-readers."
     []
     (->> "data_readers.cljc"
          io/resource
          slurp
          read-string
          (map (fn [[k v]]
                 (require (symbol (namespace v)))
                 [(str k)
                  (let [obj (gensym "obj")]

                    `(transit/read-handler
                      (fn [~obj]
                        ~((resolve v) obj))))]))
          (into {}))))
