(ns photography.appfor.io
  (:require [clojure.java.io :as io])
  (:import [java.nio.file Files]
           [com.drew.imaging ImageMetadataReader]
           [com.drew.metadata.exif ExifSubIFDDirectory]))

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
         :mimetype (Files/probeContentType (.toPath f))}
         (when-let [metadata (-> f ImageMetadataReader/readMetadata)]
           (when-let [original-time (get-original-metadata-field metadata date-function)]
             {:original-time original-time
              :original-time-offset  (get-original-metadata-field metadata offset-function)}))))

(defn raw-files-metadata [dir]
    (->> dir
        io/file
        .listFiles
        (filter #(not (.isDirectory %)))
        (map raw-file-metadata)))
