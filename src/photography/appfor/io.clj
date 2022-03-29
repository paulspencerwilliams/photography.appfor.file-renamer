(ns photography.appfor.io
  (:require [photography.appfor.utils :as u]
            [clojure.java.io :as io])
  (:import [com.drew.imaging ImageMetadataReader]
           [com.drew.metadata.exif ExifSubIFDDirectory]))

(defn expand-home [s] (clojure.string/replace-first s "~" (System/getProperty "user.home")))

(def ingest-path (expand-home "~/Pictures/photography/1-raw-ingest"))

(def back-up-path (str ingest-path "/backup"))

(def geotagged-yes-path (expand-home "~/Pictures/photography/2-possibly-geotagged/yes"))

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
  (merge {:filename (.getName f)}
         (when-let [metadata (-> f ImageMetadataReader/readMetadata)]
           (let [original-local-time  (get-original-metadata-field metadata date-function)
                 original-time-offset (get-original-metadata-field metadata offset-function)]
             {:original-utc-time    (u/to-utc original-local-time original-time-offset)
              :original-local-time  original-local-time
              :original-time-offset original-time-offset}))))

(defn raw-files-metadata [dir]
  (->> dir
       io/file
       .listFiles
       (filter #(not (.isDirectory %)))
       (map raw-file-metadata)
       (sort-by :filename)))

(defn raw-files-to-ingest []
  (->> (io/file ingest-path)
       .listFiles
       (filter #(not (or (.isDirectory %)
                         (= (.getName %) ".DS_Store"))))))

(defn ensure-back-up-dir []
  (if (not (.mkdir (io/file back-up-path)))
    (throw (Exception. (str "Could not create back up dir: " back-up-path)))))

(defn back-up-file [f]
  (io/copy f (io/file (str back-up-path "/" (.getName f)))))

(defn move-to-geotagged-yes-dir [f]
  (let [file-name      (-> f
                           raw-file-metadata
                           :original-utc-time
                           ((fn [filename] ( str  "Paul-Williams-" filename)))
                           (clojure.string/replace #":" "-"))
        file-extension (clojure.string/lower-case (last (re-matches #".*\.(.*)" (.getName f))))
        dest-path      (str geotagged-yes-path  "/" file-name "." file-extension)]
    (if (not (.renameTo f (io/file dest-path)))
      (throw (Exception. (str "Could not move file to : " dest-path))))))

(defn ensure-possibly-geotagged-dirs []
  (do (if (not (.mkdir (io/file geotagged-yes-path)))
        (throw (Exception. (str "Could not create geotagged yes path: " geotagged-yes-path)))) ))

(defn delete-back-up-dir []
  (letfn [(delete-dir [f]
            (when (.isDirectory f)
              (run! delete-dir (.listFiles f)))
            (io/delete-file f))]
    (delete-dir (io/file back-up-path))))
