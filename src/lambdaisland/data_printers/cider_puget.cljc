(ns lambdaisland.data-printers.cider-puget
  "Hook into the version of Puget that gets inlined with CIDER via MrAnderson. We
  need to scan the classpath to find the right namespaces, since they contain a
  changing version number, that's why this depends currently on
  `clojure.java.classpath`, which is currently BYO (bring-your-own). See the
  note in the README on BYO dependencies.

  This namespace is safe to load and use even if CIDER is not available on the
  classpath, in that case registering a handler will simply be a no-op."
  (:require [clojure.string :as str]
            #?(:clj [clojure.java.classpath :as cp]))
  #?(:clj (:import (java.util.jar JarFile JarEntry)
                   (java.io File))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Inlined from lambdaisland.classpath

#?(:clj
   (defn- classpath
     "clojure.java.classpath does not play well with the post-Java 9 application
  class loader, which is no longer a URLClassLoader, even though ostensibly it
  tries to cater for this, but in practice if any URLClassLoader or
  DynamicClassLoader higher in the chain contains a non-empty list of URLs, then
  this shadows the system classpath."
     []
     (distinct (concat (cp/classpath) (cp/system-classpath)))))

#?(:clj
   (defn- classpath-directories
     "Returns a sequence of File objects for the directories on classpath."
     []
     (filter #(.isDirectory ^File %) (classpath))))

#?(:clj
   (defn- classpath-jarfiles
     "Returns a sequence of JarFile objects for the JAR files on classpath."
     []
     (map #(JarFile. ^File %) (filter cp/jar-file? (classpath)))))

#?(:clj
   (defn- find-resources
     "Scan 'the classpath' for resources that match the given regex."
     [regex]
     ;; FIXME currently jar entries always come first in the result, this should be
     ;; in classpath order.
     (concat
      (sequence
       (comp
        (mapcat #(iterator-seq (.entries ^JarFile %)))
        (map #(.getName ^JarEntry %))
        (filter #(re-find regex %)))
       (classpath-jarfiles))

      (sequence
       (comp
        (mapcat file-seq)
        (map str)
        (filter #(re-find regex %)))
       (classpath-directories)))))

;; End inlining from lambdaisland.classpath
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

#?(:clj
   (defn- find-classpath-var [file-regex var-name]
     (some-> (find-resources file-regex)
             first
             (str/replace #"/" ".")
             (str/replace #"_" "-")
             (str/replace #".clj.?$" "")
             (symbol var-name)
             requiring-resolve)))

#?(:clj (def printer-ns #"cider/nrepl/inlined/deps/puget/.*/puget/printer.clj"))
#?(:clj (def dispatch-ns #"cider/nrepl/inlined/deps/puget/.*/puget/dispatch.clj"))
#?(:clj (def common-handlers-var (find-classpath-var printer-ns "common-handlers")))
#?(:clj (def tagged-handler @(find-classpath-var printer-ns "tagged-handler")))
#?(:clj (def chained-lookup @(find-classpath-var dispatch-ns "chained-lookup")))
#?(:clj (def inheritance-lookup @(find-classpath-var dispatch-ns "inheritance-lookup")))

#?(:clj (def handlers (atom {})))
#?(:clj (defonce original-handlers @common-handlers-var))

(defn register-cider-puget [type tag to-edn]
  #?@(:clj
      [(swap! handlers assoc type (tagged-handler tag to-edn))
       (alter-var-root
        common-handlers-var
        (constantly
         (chained-lookup original-handlers (inheritance-lookup @handlers))))]))
