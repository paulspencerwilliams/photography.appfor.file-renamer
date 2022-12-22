(ns photography.appfor.utils
  (:import [java.time LocalDateTime ZonedDateTime ZoneId ZoneOffset]
           [java.time.format DateTimeFormatter]))


(defn to-utc [original-time original-time-offset]
  (-> original-time
      .toInstant
      (LocalDateTime/ofInstant ZoneOffset/UTC)
      (.atZone  (if (nil? original-time-offset)
                  (ZoneId/of "Europe/London")
                  (ZoneOffset/of original-time-offset)))
      (.withZoneSameInstant (ZoneOffset/UTC))
      (.format DateTimeFormatter/ISO_DATE_TIME)))
