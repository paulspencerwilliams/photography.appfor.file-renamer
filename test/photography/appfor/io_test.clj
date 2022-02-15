(ns photography.appfor.io-test
  (:require [clojure.test :refer :all]
            [photography.appfor.io :as sut]
            [clojure.java.io :refer [resource]]))

 (deftest get-raw-files 
   (testing "Check count and name"
     (let [raw-files (->> "raw-folder"
                          resource
                          sut/raw-files-metadata) ]
       (is (= [{:filename             "scanned-file.jpg"
                :mimetype             "image/jpeg"
                :original-time        #inst "2018-05-19T07:37:00.000-00:00"
                :original-time-offset nil}
               {:filename             "jpeg-file.jpg"
                :mimetype             "image/jpeg"
                :original-time        #inst "2006-03-02T16:56:22.000-00:00"
                :original-time-offset nil}]
              raw-files)))))
