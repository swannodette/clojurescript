;; Copyright (c) Kurt Harriger. All rights reserved.
;; The use and distribution terms for this software are covered by the
;; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;; which can be found in the file epl-v10.html at the root of this distribution.
;; By using this software in any fashion, you are agreeing to be bound by
;; the terms of this license.
;; You must not remove this notice, or any other, from this software.

(ns cljs.repl.node
  (:refer-clojure :exclude [loaded-libs])
  (:require [clojure.string :as string]
            [clojure.java.io :as io]
            [cljs.compiler :as comp]
            [cljs.repl :as repl])
  (:import cljs.repl.IJavaScriptEnv
           java.net.Socket))


 (defn socket [host port]
   (let [socket (java.net.Socket. host port)
         in (java.io.BufferedReader. 
             (java.io.InputStreamReader. (.getInputStream socket)))
         out (java.io.PrintWriter. (.getOutputStream socket))]
     {:in in :out out}))

(defn close-socket [s]
  (.close (:in s))
  (.close (:out s)))

(defn eval [s str]
  (.write (:out s) str)
  (.flush (:out s))
  (.readLine (:in s)))


(extend-protocol repl/IJavaScriptEnv
  clojure.lang.IPersistentMap
  (-setup [this]
    (assoc this :socket (socket (:host this) (:port this))))
  (-evaluate [this filename line js]
    (println js)
    {:status :error :value (eval (:socket this) js)})
  (-load [this ns url]
    nil)
  (-tear-down [this] (close-socket (:socket this))))

(defn repl-env
  "Returns a fresh JS environment, suitable for passing to repl.
  Hang on to return for use across repl calls."
  [& {:as opts}] (merge {:host "localhost" :port 5001} opts))

