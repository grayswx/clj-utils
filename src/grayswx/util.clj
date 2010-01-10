;; A set of general utility functions and macros.
(ns grayswx.util
  (:use [clojure.test]
        [clojure.contrib.def]
        [clojure.contrib.map-utils]))

(with-test
  (defmacro inject-argument
    "Takes a form and injects arg as the first argument to it.
If form is a function, returns (form arg).
If form is a seq, it inserts arg as the second item of form."
    [form arg]
    (if (seq? form)
      `(~(first form)
        ~arg
        ~@(rest form))
      `(~form ~arg)))
  (is (= (/ 4) (inject-argument (/) 4)) "Single argument injection.")
  (is (= (/ 4) (inject-argument / 4)) "Cast to form.")
  (is (= (+ 1 2 3) (inject-argument (+ 2 3) 1)) "Multiple arguments."))

(with-test
  (defmacro doto-reduce
    "Takes val and places it as the first argument to the
first form, takes that result and places it as the first
argument to the second form, etc..."
    [val & forms]
    (if forms
      `(doto-reduce
         ~`(inject-argument ~(first forms) ~val)
         ~@(rest forms))
      val))
  (is (= {:a 1 :b 2}
         (doto-reduce {} (assoc :a 1 :b 2)))
      "Single action.")
  (is (= {:a 1 :b 2}
         (doto-reduce {} (assoc :a 1) (assoc :b 2)))
      "Double action.")
  (is (= 3 (doto-reduce [] (conj 1) (conj 2) (conj 3) count))
      "No argument."))

(with-test
  (defmacro if-key
    "If the supplied map has the given key, inject it as the first argument
of the given form.  Otherwise, execute the else branch."
    [map key then & else]
    (let [val 'val#]
      `(if-let [~val (get ~map ~key)]
         (inject-argument ~then ~val)
         (do ~@else))))
  (is (= (if-key {:a 2} :a / 7)
         (/ 2)))
  (is (= (if-key {:b 4} :a inc 4)
         4)))

(with-test
  (defmacro inject-key
    "Injects the value of a map as the first argument to a form."
    ([map key form]
       `(inject-key ~map ~key nil ~form))
    ([map key default form]
       `(inject-argument ~form (get ~map ~key ~default))))
  (is (= 2 (inject-key {:a '(1 2)} :a count)))
  (is (= 8 (inject-key {:b 4} :a 7 inc))))


(with-test
  (defmacro modify-key
    "Takes (get map key default) and places it as the first argument of form.
Evaluates the form, and puts the result back into map at the specified key."
    {:arglist '([map key default form] [map key form])}
    [map key & args]
    (list 'assoc map key
          `(inject-key ~map ~key ~@args)))
  (is (= {:a 4 :b 1} (modify-key {:a 1 :b 1} :a (+ 3))))
  (is (= {:a 4 :b 1} (modify-key {:b 1} :a 3 inc))))

(defmacro reduce-key
  "Equivalent to (modify-key map key default (doto-reduce ...))"
  [map key default & forms]
  `(modify-key ~map ~key ~default (doto-reduce ~@forms)))

(with-test
  (defn holds?
   "Checks a collection to see if it holds the specified value."
   [coll val]
   (some #{val} coll))
  (is (holds? #{:a :b :c} :a))
  (is (holds? [:a :b :c] :c))
  (is (not (holds? '(:a :b :c) :d))))

(with-test
  (defn deep-merge
   "Recursively merge maps."
   [& maps]
   (apply clojure.contrib.map-utils/deep-merge-with (fn [a b] b) maps))
  (is (= {:a {:b 2 :c 3} :d 4}
         (deep-merge {:a {:b 2 :c 2}}
           {:a {:c 3}}
           {:d 4}))))

(with-test
  (defn map-vals
    "Like map, except it affects the values of a hashmap
instead of the entire collection."
    [m f]
    (apply hash-map
      (interleave
       (keys m)
       (map (fn [[k v]] (f v))
         m))))
  (let [m {1 2 3 4 5 6}]
    (is (= m (map-vals m identity))
        "Identity function changes map.")
    (is (= {1 3 3 5 5 7} (map-vals m inc))
        "Increment does not work.")))

