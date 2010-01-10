(ns grayswx.util.vector)

(defn make-matrix
  "Creates a matrix of the specified dimensions.
Specify the outermost dimension first."
  [init dim & dims]
  (vec
   (repeat dim
           (if (empty? dims)
             init
             (apply make-matrix init dims)))))