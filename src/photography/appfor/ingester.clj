(ns photography.appfor.ingester
  (:require [photography.appfor.io :as io]))

(defn foo
  "I don't do a whole lot."
  [_]
  (let [to-ingest (->> (io/raw-files-to-ingest)
                       (io/with-unique-destination-filenames))]
    (do (io/ensure-back-up-dir)
        (doall (map #(do (io/back-up-file %)
                         (println (str "Backed up file                               " %))
                         ;;(io/copy-to-originals-dir %)
                         (println (str "Move to originals " %))
                         (io/move-to-capture-one-dir %)
                         (println (str "Move for capture one ingest " %)))
                    to-ingest))
        (io/delete-back-up-dir))))
