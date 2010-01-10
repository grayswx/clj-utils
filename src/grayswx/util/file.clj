;;; Utility Functions for finding and manipulating files.
(ns grayswx.util.file
  (:use [clojure.test]
        [clojure.contrib.def]
        [grayswx.util.string]
        [grayswx.util.cast]
        [grayswx.util.os]) 
  (:import (java.io File)
           (java.util.regex Pattern)))

;; Like the string function, but treats the objects as part of a path.
(with-test
  (defn path-str
    "Merges strings as path components."
    [& paths]
    (let [pathlist (filter #(< 0 (.length %)) paths)
          tail (rest pathlist)]
      (if-not (empty? pathlist)
        (if (and (not (some #(= File (type %)) tail))
                 (some #(re-find root-re %) tail))
          (throw (RuntimeException. (str "path-str:  One or more of the "
                                         "additional path components is "
                                         "absolute.")))
          (let [path
                (apply str (map #(if-not
                                     (.endsWith (->String %) File/separator)
                                   (str % File/separator)
                                   %)
                                (butlast pathlist)))]
            (str path (last pathlist)))))))
  (cond
    ;; Linux paths.
    (= os :lin)
    (do (is (= "a/b" (path-str "a" "b")))
        (is (= "/a/hello" (path-str "/" "a" "hello")))
        (is (= "/" (path-str "/")))
        (is (thrown? RuntimeException (path-str "h" "/b"))))
    ,,,,,))

(defn- re-glob
  "Creates a regular expression from a glob."
  [glob]
  (let [re-str (.replace glob "." "\\.")
        re-str (.replace re-str "*" ".*")
        re-str (str "^" re-str "$")]
    (re-pattern re-str)))

(defn- list-wild
  "Lists all files in a directory which match the given pattern."
  [dir fnm]
  (filter #(re-find (re-glob fnm) %)
          (.list (->File dir))))

(defn- split-path
  "Splits a path into its component parts."
  [path]
  (let [ary (.split path File/separator)]
    (if (.startsWith path "/")
      (aset ary 0 "/"))
    (seq ary)))

(defn- glob-
  [dir f-path r-paths]  
  (cond
    (not f-path)
    (let [file (File. dir)]
      (if (.exists file)
        (list file)))
    ,,,,,
    (.contains f-path "*")
    (mapcat glob-
            (repeat dir)
            (list-wild dir f-path)
            (repeat r-paths))
    ,,,,,
    true
    (recur (path-str dir f-path)
           (first r-paths)
           (rest r-paths))))

(defn glob
  "Lists all files matching a pattern."
  [glob]
  (let [path-list (split-path glob)
        glob-list (glob- "" (first path-list) (rest path-list))]
    (if glob-list
      glob-list
      (list))))

(with-test
  (defn get-extension
    "Returns the extension of a file."
    [#^File file]
    (let [s (.getName (->File file))]
      (if-let [i (index-of s ".")]
        (.substring s (inc i)))))
  (is (= "jpg" (get-extension (File. "/hello/world.jpg"))))
  (is (= "tar.bz2" (get-extension (File. "war.tar.bz2"))))
  (is (not (get-extension (File. "hello")))))
