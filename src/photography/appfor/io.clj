(ns photography.appfor.io
  (:require [photography.appfor.utils :as u]
            [clojure.java.io :as io]
            [clojure.string :refer [ends-with? upper-case]])
  (:import [com.drew.imaging ImageMetadataReader]
           [com.drew.metadata.exif ExifSubIFDDirectory]))

(defn expand-home [s] (clojure.string/replace-first s "~" (System/getProperty "user.home")))

(def ingest-path (expand-home "~/Pictures/photography/1-raw-ingest"))

(def back-up-path (str ingest-path "/backup"))

(def original-path "/Volumes/RAID/photography/elete me")

(def capture-one-path (expand-home "~/Pictures/photography/2-for-capture-one"))

(defn date-function [d] [(.getDate d ExifSubIFDDirectory/TAG_DATETIME_ORIGINAL)
                         (.getDate d ExifSubIFDDirectory/TAG_DATETIME_DIGITIZED)])

(defn offset-function [d] [(.getString d ExifSubIFDDirectory/TAG_TIME_ZONE_ORIGINAL)
                           (.getString d ExifSubIFDDirectory/TAG_TIME_ZONE_DIGITIZED)])

(defn get-original-metadata-field [metadata, field-fn]
  (->> metadata
       .getDirectories
       (filter #(instance? ExifSubIFDDirectory %))
       (mapcat field-fn)
       (some identity)))

(defn raw-file-metadata [f]
  (merge {:filename (.getName f)
          :file f}
         (when-let [metadata (-> f ImageMetadataReader/readMetadata)]
           (let [original-local-time  (get-original-metadata-field metadata date-function)
                 original-time-offset (get-original-metadata-field metadata offset-function)]
             {:original-utc-time    (u/to-utc original-local-time original-time-offset)
              :original-local-time  original-local-time
              :original-time-offset original-time-offset}))))

(defn raw-files-to-ingest []
  (->> (io/file ingest-path)
       .listFiles
       (filter #(not (or (.isDirectory %)
                         (= (.getName %) ".DS_Store")
                         (ends-with? (upper-case (.getName %)) ".XMP"))))
       (map raw-file-metadata)
       (sort-by :filename)))

(defn destination-filename
  ([m i]
   (let [file-name      (-> m
                            :original-utc-time
                            ((fn [filename] (str  "Paul-Williams-" filename)))
                            (clojure.string/replace #":" "-"))
         file-extension (clojure.string/lower-case (last (re-matches #".*\.(.*)" (:filename m))))]
     (assoc m :destination-filename (str file-name (when i (str "-" (format "%03d" i))) "." file-extension))))
  ([m] (destination-filename m nil)))

(defn with-unique-per-second-destination-filename [s]
  (->> s
       (sort-by :filename)
       (map-indexed (fn [i m] (destination-filename m (inc i))))))

(defn ensure-back-up-dir []
  (if (not (.mkdir (io/file back-up-path)))
    (throw (Exception. (str "Could not create back up dir: " back-up-path)))))

(defn back-up-file [m]
  (io/copy (:file m) (io/file (str back-up-path "/" (:filename m)))))

(defn with-unique-destination-filenames [s]
    (->> s
       (group-by :original-utc-time)
       (map (fn [me]
                 (if (= 1 (count (val me)))
                   (destination-filename (first (val me)))
                   (with-unique-per-second-destination-filename (val me))
                   )))
       flatten
       (sort-by :filename)))

(defn copy-to-originals-dir [m]
  (io/copy (:file m) (io/file (str original-path "/" (:destination-filename m)))))

(defn move-to-capture-one-dir [m]
  (let [dest-path (str capture-one-path "/" (:destination-filename m))]
    (if (not (.renameTo (:file m) (io/file dest-path)))
      (throw (Exception. (str "Could not move file to : " dest-path))))))

(defn delete-back-up-dir []
  (letfn [(delete-dir [f]
            (when (.isDirectory f)
              (run! delete-dir (.listFiles f)))
            (io/delete-file f))]
    (delete-dir (io/file back-up-path))))
