(ns lambdaisland.data-printers.cider-puget
  "Hook into the version of Puget that gets inlined with CIDER via MrAnderson. We
  need to scan the classpath to find the right namespaces, since they contain a
  changing version number, that's why this depends currently on
  `lambdaisland.classpath`, which is currently BYO (bring-your-own): it is not
  listed as an explicit dependency because it is (transitively) a large
  dependency. Note that we may drop this dependency again, since we don't need
  all of tools.deps here."
  (:require [lambdaisland.classpath :as licp]
            [clojure.string :as str]))

#?(:clj
   (defn find-classpath-var [file-regex var-name]
     (some-> (licp/find-resources file-regex)
             first
             (str/replace #"/" ".")
             (str/replace #"_" "-")
             (str/replace #".clj.?$" "")
             (symbol var-name)
             requiring-resolve)))

#?(:clj (def printer-ns #"cider/nrepl/inlined_deps/puget/.*/puget/printer.clj"))
#?(:clj (def dispatch-ns #"cider/nrepl/inlined_deps/puget/.*/puget/dispatch.clj"))
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
