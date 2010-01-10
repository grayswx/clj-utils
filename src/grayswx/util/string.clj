;;: A number of utility functions for the manipulation of strings.
(ns grayswx.util.string
  (:use [clojure.test]
        [grayswx.util.cast]))

;; Replacement functions for String#indexOf and String#lastIndexOf,
;; to make them friendly and return nil instead of -1 if nothing is found.

(with-test
  (defn index-of
    "Returns the index of the substring in a string,
 or nil if it isn't there."
    [string substr]
    (let [index (.indexOf string substr)]
      (if (<= 0 index)
        index)))
  (is (= 3 (index-of "lemon" "o")))
  (is (= 0 (index-of "alpha" "a")))
  (is (not (index-of "power" "q"))))

(with-test
  (defn last-index-of
    "Returns the last index of the substring in a string,
or nil if it isn't there."
    [str substr]
    (let [index (.lastIndexOf str substr)]
      (if (<= 0 index)
        index)))
  (is (= 3 (last-index-of "lemon" "o")))
  (is (= 3 (last-index-of "hello" "l")))
  (is (not (last-index-of "quark" "s"))))


