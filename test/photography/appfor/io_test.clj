(ns photography.appfor.io-test
  (:require [clojure.test :refer :all]
            [photography.appfor.io :as sut]
            [clojure.java.io :as io]))

(deftest get-raw-files 
  (testing "Check count and name"
    (let [raw-files (->> "raw-folder"
                         io/resource
                         sut/get-raw-files) ]
      (is (= [(->> "raw-folder/raw-file.JPG"
                   io/resource
                   .toURI
                   java.io.File.)]
             raw-files)))))
