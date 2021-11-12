(ns lambdaisland.data-printers
  "Provide a convenient interface for defining tagged print handlers for custom
  types. All `register-...` functions take the same arguments: `[type tag to-edn]`

  - `type` the type this handler is for (`java.lang.Class` or JS constructor)
  - `tag` the symbol used as tag, without the `#`
  - `to-edn` a function which takes the object to be printed, and returns plain
    data (vectors, maps, etc)
  "
  (:require [clojure.pprint :as pprint]))

(defn type-name
  "Given a class/type, return its name as a string. For ClojureScript this is a
  best effort, and you may need to give it a hand by setting a `.-name` property
  on your constructor function."
  [t]
  #?(:clj
     (.getName ^Class t)
     :cljs (let [n (.-name t)]
             (if (empty? n)
               (symbol (pr-str t))
               (symbol n)))))

#?(:cljs
   (do
     ;; Monkeypatch pprint so it's extensible by type
     (defmulti type-name-pprint-dispatch (comp type-name type))

     (defmethod type-name-pprint-dispatch :default [obj]
       (-write *out* (pr-str obj)))

     (defn- cljs-pprint-recognized? [obj]
       (or (instance? PersistentQueue obj)
           (satisfies? IDeref obj)
           (symbol? obj)
           (seq? obj)
           (map? obj)
           (vector? obj)
           (set? obj)
           (nil? obj)))

     (pprint/set-pprint-dispatch
      (fn [obj]
        (if (cljs-pprint-recognized? obj)
          (pprint/simple-dispatch obj)
          (type-name-pprint-dispatch obj))))))

(defn- use-method
  "Installs a function as a new method of multimethod associated with dispatch-value."
  [multifn dispatch-val func]
  #?(:clj (.addMethod ^clojure.lang.MultiFn multifn dispatch-val func)
     :cljs (-add-method multifn dispatch-val func)))

(defn register-print
  "Register a regular print handler. Extends `clojure.core/print-method` and
  `clojure.core/print-dup` on Clojure, and implements `IPrintWithWriter` on
  ClojureScript."
  [type tag to-edn]
  #?(:clj
     (let [print-handler (fn [obj ^java.io.Writer w]
                           (.write w (str "#" tag " " (pr-str (to-edn obj)))))]
       (use-method print-method type print-handler)
       (use-method print-dup type print-handler))

     :cljs
     (extend-type type
       IPrintWithWriter
       (-pr-writer [obj w opts]
         (-write w (str "#" tag " "))
         (pr-seq-writer [(to-edn obj)] w (assoc opts :readably true))))))

(defn register-pprint
  "Register pretty-print writer based on a class/type."
  [type tag to-edn]
  #?(:clj
     (use-method pprint/simple-dispatch
                 type
                 (fn [obj]
                   (print (str "#" tag " "))
                   (pprint/write-out (to-edn obj))))

     :cljs
     (use-method type-name-pprint-dispatch
                 (symbol (type-name type))
                 (fn [obj]
                   (-write *out* (str "#" tag " "))
                   (binding [*print-readably* true]
                     (pprint/write-out (to-edn obj)))))))
