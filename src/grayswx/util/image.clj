;;; General Image Utilities.
(ns grayswx.util.image
  (:use [clojure.test])
  (:import (java.awt Color)
           (java.awt.image AffineTransformOp BufferedImage)
           (java.awt.geom AffineTransform)))

(defn replace-color
  "Replaces one color with another."
  [image #^Color old #^Color new]
  (let [width (.getWidth image)
        height (.getHeight image)
        size (* width height)
        pixels (make-array Integer/TYPE size)
        old-int (.getRGB old)
        new-int (.getRGB new)]
    (.getRGB image 0 0 width height pixels 0 width)
    (loop [index 0]
      (when (< index size)
        (if (= (aget pixels index) old-int)
          (aset pixels index new-int))
        (recur (inc index))))
    (.setRGB image 0 0 width height pixels 0 width)))

(with-test
  (defn scale-image
    "Scales an image to fit in the specified size."
    [img w h]
    (let [x (.getWidth img)
          y (.getHeight img)
          sr (/ x y)                     ; Source dimension ratio.
          tr (/ w h)                     ; Target dimension ratio.
          r (if (> tr sr)
              (/ h y)
              (/ w x))]
      (.filter (AffineTransformOp. (doto (AffineTransform.) (.scale r r))
                                   AffineTransformOp/TYPE_BILINEAR)
               img nil)))
  (let [scale-info #(let [im (scale-image %1 %2 %3)]
                      [(.getWidth im) (.getHeight im)])
        img-square (BufferedImage. 100 100 BufferedImage/TYPE_INT_ARGB)
        img-rect (BufferedImage. 200 100 BufferedImage/TYPE_INT_ARGB)]
    (is (= [40 40] (scale-info img-square 40 40)))
    (is (= [40 40] (scale-info img-square 200 40)))
    (is (= [80 40] (scale-info img-rect 1000 40)))
    (is (= [80 40] (scale-info img-rect 80 1000)))))

(defn rotate-image
  "Rotates an image."
  [img deg]
  (.filter (AffineTransformOp. (doto (AffineTransform.)
                                 (.rotate (/ (* deg Math/PI) 180)
                                          (/ (.getWidth img) 2)
                                          (/ (.getHeight img) 2)))
                               AffineTransformOp/TYPE_BILINEAR)
           img nil))