(ns photography.appfor.io
  (:require [clojure.java.io :as io])
  (:import [java.nio.file Files]
           [com.drew.imaging ImageMetadataReader]
           [com.drew.metadata.exif ExifSubIFDDirectory]))


(defn raw-files-metadata [dir]
    (->> dir
        io/file
        .listFiles
        (filter #(not (.isDirectory %)))
        (map #(merge {:filename (.getName %)
                      :mimetype (Files/probeContentType (.toPath %))}
                     (when-let [metadata (-> % ImageMetadataReader/readMetadata)]
                       (when-let [exif-metadata (.getFirstDirectoryOfType metadata ExifSubIFDDirectory)]
                         {:original-time        (some identity [(.getDate exif-metadata ExifSubIFDDirectory/TAG_DATETIME_ORIGINAL)
                                                                (.getDate exif-metadata ExifSubIFDDirectory/TAG_DATETIME_DIGITIZED)])
                          :original-time-offset (.getDate exif-metadata ExifSubIFDDirectory/TAG_TIME_ZONE_ORIGINAL)}))))))
