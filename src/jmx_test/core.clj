(ns jmx-test.core
  (:require
   [clojure.contrib.jmx :as jmx]))

(def *core-stats* (ref { :a-number 0 })

(defn register-jmx []
  (log/debug "starting jmx")
  (jmx/register-mbean 
   (clojure.contrib.jmx.Bean. *core-stats*) "raven:type=Core"))

