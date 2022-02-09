(ns photography.appfor.io-test
  (:require [clojure.test :refer :all]
            [clojure.java.io :as io]))

(deftest get-raw-files 
  (testing "Check count and name"
    (let [raw-files (->> "raw-folder"
                         io/resource
                         io/file
                         file-seq
                         (filter #(.isDirectory %)))]
      (is (= 1 (count raw-files))))))
