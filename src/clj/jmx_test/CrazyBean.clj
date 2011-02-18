
(ns jmx-test.CrazyBean
  (:gen-class
   :implements [javax.management.DynamicMBean]
   :init init
   :state state
   :constructors {[Object] []})
  (:require [clojure.contrib.jmx :as jmx])
  (:use [clojure.contrib.str-utils :only (re-sub re-split)])
  (:import [javax.management DynamicMBean MBeanInfo AttributeList
            MBeanOperationInfo MBeanParameterInfo]))

(defn -init [derefable]
  [[] derefable])

;; util
(defn zip [& cols] (apply map vector cols))

(defn unmangle
"Given the name of a class that implements a Clojure function, returns the function's name in Clojure. Note: If the true Clojure function name
  contains any underscores (a rare occurrence), the unmangled name will
  contain hyphens at those locations instead."
  [class-name]
  (.replace
   (re-sub #"^(.+)\$(.+)__\d+$" "$1/$2" class-name)
   \_ \-))

(defn fn-by-name
  "lookup string to fn at run time"
  [ns-symbol fn-name]
  (intern (clojure.lang.Namespace/find ns-symbol) (symbol (name fn-name))))
;; /util

(defn build-parameter-info [param opts]
  (let [{:keys [name type description]
         :or {name  (.getName param)
              type  (.getName param)
              description ""}} opts]
    (MBeanParameterInfo. name type description)))

(defn parameter-infos [params-seq]
  (into-array MBeanParameterInfo
   (map 
    (fn [[i param]] 
      (prn [i param])
      (build-parameter-info param {:name (str "p" i)})) 
    (zip (range 0 (count params-seq)) params-seq))))

(defn build-operation-info [ifn opts]
  (prn (.getName (class ifn)))
  (let [{:keys [name description argv return-type impact]
         :or {name         (unmangle (.getName (class ifn))) 
              description  (unmangle (.getName (class ifn)))
              argv         nil
              return-type  "void"
              impact       MBeanOperationInfo/ACTION}} opts
         params (parameter-infos argv)]
   (MBeanOperationInfo. name description params return-type impact)))

(defn operation-infos [ops-seq]
  (into-array (map (fn [[ifn opts]] (build-operation-info ifn opts))
                   ops-seq)))

; TODO: rest of the arguments, as needed
(defn generate-mbean-info [clj-bean]
  (MBeanInfo. 
   (.. clj-bean getClass getName)                ; class name
   "Clojure Dynamic MBean"                       ; description
   (jmx/map->attribute-infos (dissoc @(.state clj-bean) :operations)) ;; attributes
   nil                                           ; constructors
   (operation-infos (@(.state clj-bean) :operations)) ; operations
   nil))                                         ; notifications                                          
(defn -getMBeanInfo
  [this]
  (generate-mbean-info this))

(defn -getAttribute
  [this attr]
  (@(.state this) (keyword attr)))

(defn -getAttributes
  [this attrs]
  (let [result (AttributeList.)]
    (doseq [attr attrs]
      (.add result (.getAttribute this attr)))
    result))

(defn -invoke [this name args sig]
  ;; who knows what kind of havoc this could wreak!
  (prn ["invoke " name args sig])
  (let [[ns-name fn-name] (re-split #"\$" name)
        ifn (fn-by-name (symbol ns-name) fn-name)]
    (apply ifn (seq args))))
