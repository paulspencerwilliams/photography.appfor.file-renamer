(ns photography.appfor.ingester
  (:require [photography.appfor.io :as io]))

(defn foo
  "I don't do a whole lot."
  [x]
  (let [to-ingest (io/raw-files-to-ingest)]
    (do (io/ensure-back-up-dir)
        (io/ensure-possibly-geotagged-dirs)
        (doall (map #(do (io/back-up-file %)
                         (println (str "Backing up file                              " %))
                         (io/move-to-geotagged-yes-dir %)
                         (println (str "Move ASSUMED geotagged file to geotagged/yes " %))) to-ingest))
        (io/delete-back-up-dir))))
