(defproject jmx-test "1.0.0-SNAPSHOT"
  :description "FIXME: write"
  :dependencies [[org.clojure/clojure "1.2.0"]
                 [org.clojure/clojure-contrib "1.2.0"]]
  :dev-dependencies
    [[lein-javac "1.2.1-SNAPSHOT"]
     [swank-clojure "1.3.0-SNAPSHOT"]
     [robert/hooke "1.0.2"]
     [lein-daemon "0.2.1"]]
  :compile-path "build/classes"
  :target-dir "build"
  :java-source-path "src/java"
  :source-path "src/clj"
  :jvm-opts ["-Dcom.sun.management.jmxremote"
             "-Dcom.sun.management.jmxremote.local.only=false"
             "-Dcom.sun.management.jmxremote.port=7676"
             "-Dcom.sun.management.jmxremote.authenticate=false"
             "-Dcom.sun.management.jmxremote.ssl=false"]
  :aot [jmx-test.CrazyBean]
  )
