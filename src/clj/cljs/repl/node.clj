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
         in (.getInputStream socket)
         out (java.io.PrintWriter. (.getOutputStream socket))]
     {:socket socket :in in :out out}))

(defn close-socket [s]
  (.close (:in s))
  (.close (:out s))
  (.close (:socket s)))

(defn read-available [in]
  (let [sb (java.lang.StringBuilder.)]
    (while (> (.available in) 0)
      (.append sb (char (.read in))))
    (str sb)))

(defn read-response [in]
  (comment wait till data is available before trying to read
           (while (= (.available in) 0))
           (comment give it a second to write results)
           (Thread/sleep 1000))
  (read-available in))

(defn write-socket [out str]
  (doto out (.write str) (.flush)))

(defn setup [ctx]
  (comment  (println ctx)
            (repl/load-file ctx "cljs/nodejs.cljs")
            (repl/load-file ctx "cljs/core.cljs")))

(defn node-eval [ctx js]
  (write-socket (:out ctx) js)
  {:status :error :value (read-response (:in ctx))})

(defn load-javascript [ctx ns url]
  (node-eval ctx (slurp url)))


(extend-protocol repl/IJavaScriptEnv
  clojure.lang.IPersistentMap
  (-setup [this] (setup this))
  (-evaluate [this filename line js] (node-eval this js))
  (-load [this ns url] (load-javascript this ns url))
  (-tear-down [this] (close-socket this)))

(defn repl-env
  "Returns a fresh JS environment, suitable for passing to repl.
  Hang on to return for use across repl calls."
  [& {:as opts}] (let
                     [newopts (merge {:host "localhost" :port 5001} opts)]
                   (merge (socket (:host newopts) (:port newopts)) newopts)))
