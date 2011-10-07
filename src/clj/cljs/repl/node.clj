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
  (:import cljs.repl.IJavaScriptEnv))


(extend-protocol repl/IJavaScriptEnv
  clojure.lang.IPersistentMap
  (-setup [this]
    nil)
  (-evaluate [this filename line js]
    {:status :error :value "not implemented"})
  (-load [this ns url]
    nil)
  (-tear-down [_] nil))

(defn repl-env
  "Returns a fresh JS environment, suitable for passing to repl.
  Hang on to return for use across repl calls."
  [& {:as opts}] (merge {:port 5001} opts))

