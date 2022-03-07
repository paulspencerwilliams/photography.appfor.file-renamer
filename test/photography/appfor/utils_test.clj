(ns photography.appfor.utils-test
  (:require [clojure.test :refer :all]
            [photography.appfor.utils :as sut]
            [time-literals.data-readers]
            [time-literals.read-write]))

(time-literals.read-write/print-time-literals-clj!)

(deftest to-utc
  (testing "from local time zone with no offset assumes Europe/London"
    (is (= #time/zoned-date-time "2022-03-07T17:18:32Z"
           (sut/to-utc #inst "2022-03-07T17:18:32" nil)))
    (is (= #time/zoned-date-time "2022-04-07T16:18:32Z"
           (sut/to-utc #inst "2022-04-07T17:18:32" nil))))
  (testing "from local time zone with offset"
    (is (= #time/zoned-date-time "2022-03-07T17:18:32Z"
           (sut/to-utc #inst "2022-03-07T17:18:32" "+00:00")))
    (is (= #time/zoned-date-time "2022-04-07T16:18:32Z"
           (sut/to-utc #inst "2022-04-07T18:18:32" "+02:00")))))
