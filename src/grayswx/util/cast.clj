; A library consisting of functions to coerce types.
(ns grayswx.util.cast
  (:import (java.io File)))

(defmulti #^File ->File class)
(defmethod ->File File [#^File f] f)
(defmethod ->File String [#^String s]
  (File. (if (.startsWith s "~")
           (.replace s "~" (System/getenv "HOME"))
           s)))

(defmulti #^String ->String class)
(defmethod ->String String [#^String s] s)
(defmethod ->String clojure.lang.Keyword [#^clojure.lang.Keyword k]
  (.substring (str k) 1))
(defmethod ->String File [#^File f] (.getAbsolutePath f))

(defmulti #^clojure.lang.PersistentHashSet ->Set class)
(defmethod ->Set clojure.lang.PersistentHashSet [s] s)
(defmethod ->Set nil [] #{})
(defmethod ->Set String [#^String s] #{s})

(defmulti #^clojure.lang.PersistentList ->List class)
(defmethod ->List clojure.lang.PersistentList [l] l)
(defmethod ->List :default [i] (list i))