(ns photography.appfor.io
  (:require [clojure.java.io :as io]))

(defn get-raw-files [dir]
  (->> dir
      io/file
      .listFiles
      (filter #(not (.isDirectory %)))))
