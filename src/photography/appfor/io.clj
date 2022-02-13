(ns photography.appfor.io
  (:require [clojure.java.io :as io])
  (:import [com.drew.imaging ImageMetadataReader]
           [com.drew.metadata.exif ExifSubIFDDirectory]))

(defn raw-files-metadata [dir]
    (->> dir
        io/file
        .listFiles
        (filter #(not (.isDirectory %)))
        (map #(let [metadata
                   (-> % 
                       ImageMetadataReader/readMetadata
                       (.getFirstDirectoryOfType ExifSubIFDDirectory))]
               {:filename             (.getName %)
                :original-time        (.getDate metadata ExifSubIFDDirectory/TAG_DATETIME_ORIGINAL)
                :original-time-offset (.getDate metadata ExifSubIFDDirectory/TAG_TIME_ZONE_ORIGINAL)}))))
