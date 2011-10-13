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
            [cljs.repl :as repl]
            [cljs.closure :as cljsc])
  (:import cljs.repl.IJavaScriptEnv
           java.net.Socket))

(def current-repl-env (atom nil))
(def loaded-libs (atom #{}))


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

(defn write-socket [out ^String s]
  (doto out (.println s) (.flush)))


(defn node-eval [ctx js]
  (write-socket (:out ctx) js)
  {:status :error :value (read-response (:in ctx))})

(defn load-javascript [ctx ns url]
  (node-eval ctx (slurp url)))


(defn setup [repl-env]
  (let [env {:context :statement :locals {} :ns (@comp/namespaces comp/*cljs-ns*)}
        scope (:scope repl-env)]
    (repl/load-file repl-env "cljs/core.cljs")
    (swap! loaded-libs conj "cljs.core")))

(extend-protocol repl/IJavaScriptEnv
  clojure.lang.IPersistentMap
  (-setup [this] (setup this))
  (-evaluate [this filename line js] (node-eval this js))
  (-load [this ns url] (load-javascript this ns url))
  (-tear-down [this] (close-socket this)))


(defn repl-env
  "Returns a fresh JS environment, suitable for passing to repl.
  Hang on to return for use across repl calls."
  [& {:keys [host port] :or {host "localhost" port 5001}}]
  (let [repl-env (socket host port)
        base (io/resource "goog/base.js")
        deps (io/resource "goog/deps.js")]
    (node-eval repl-env  (slurp  (io/reader base)))
    (node-eval repl-env  (slurp  (io/reader deps)))
    repl-env))
