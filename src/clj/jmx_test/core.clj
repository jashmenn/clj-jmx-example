(ns jmx-test.core
  (:require
   [clojure.contrib.jmx :as jmx])
  (:import [jmx_test CrazyBean]))

(declare set-jmx-stat)

(defn say-hi []
  (println "hello world"))

(defn say-something [phrase]
  (println phrase))

(defn reset-counter []
  (set-jmx-stat :core :a-number 0))

(def *core-stats* (ref { 
   :a-number 0 
   :operations [[say-hi {:name "say-hi"
                         :description "says 'hello world'"}]
                [reset-counter {:name "reset-counter"}]
                [say-something {:name "say-something"
                                :argv [String]}]
                ]}))

(def jmx-ns {:core *core-stats*})

(defn set-jmx-stat [ns key value]
  (dosync
   (ref-set (jmx-ns ns) 
            (assoc @(jmx-ns ns) key value))))

(defn set-jmx-stat-with-fn [ns key f]
  (dosync
   (ref-set (jmx-ns ns) 
            (assoc @(jmx-ns ns) key 
                   (f (@(jmx-ns ns) key))))))

(defn inc-jmx [ns key]
  (set-jmx-stat-with-fn ns key inc))

(defn register-jmx []
  (println "starting jmx")
  (jmx/register-mbean 
   (CrazyBean. *core-stats*) "jmx-example:type=Core"))

(defn tick []
  (inc-jmx :core :a-number)
  (. Thread (sleep 1000))
  (recur))

(defn -main [& args]
  (register-jmx)
  (.start (Thread. tick))
  (. Thread (sleep 10000000000)))
