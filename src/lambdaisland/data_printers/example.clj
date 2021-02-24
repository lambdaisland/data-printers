(ns lambdaisland.data-printers.example
  (:require [lambdaisland.data-printers :as dp]
            [lambdaisland.data-printers.deep-diff :as dp-ddiff]
            [lambdaisland.data-printers.deep-diff2 :as dp-ddiff2]
            [lambdaisland.data-printers.transit :as dp-transit]
            [lambdaisland.data-printers.puget :as dp-puget]))

(defn register-printer [type tag to-edn]
  (dp/register-print type tag to-edn)
  (dp/register-pprint type tag to-edn)
  (dp-puget/register-puget type tag to-edn)
  (dp-ddiff/register-deep-diff type tag to-edn)
  (dp-ddiff2/register-deep-diff2 type tag to-edn)
  (dp-transit/register-write-handler type tag to-edn))
