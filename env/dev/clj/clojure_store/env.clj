(ns clojure-store.env
  (:require
    [selmer.parser :as parser]
    [clojure.tools.logging :as log]
    [clojure-store.dev-middleware :refer [wrap-dev]]))

(def defaults
  {:init
   (fn []
     (parser/cache-off!)
     (log/info "\n-=[clojure-store started successfully using the development profile]=-"))
   :stop
   (fn []
     (log/info "\n-=[clojure-store has shut down successfully]=-"))
   :middleware wrap-dev})
