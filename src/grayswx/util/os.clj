;;; OS specific things.
(ns grayswx.util.os
  (:use [clojure.test]
        [grayswx.util.debug]))

(def os (some (fn [[re res]]
                (if (re-find re (System/getProperty "os.name")) res))
              {#"(?i:windows)" :win,
               #"(?i:linux)"   :lin,
               #"(?i:mac)"     :mac,
               #".*"           :???}))

(def root-re ({:win #"^[A-Z]\:\\"
               :lin #"^/"}
              os))

(def root ({:win "C:"
            :lin "/"} os))