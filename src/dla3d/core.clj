(ns dla3d.core
  (:require
    [quil.core :as q]
    [quil.middleware :as m]
    [kdtree :as kd]
    ))

(defn new-rand-point [xyz-range]
  [(- (rand (* 2 xyz-range)) xyz-range)
   (- (rand (* 2 xyz-range)) xyz-range)
   (- (rand (* 2 xyz-range)) xyz-range)])

(defn stick-particle [tree]
  (let [sz 250
        radius 5.0
        touch-dist-sq (* 2 radius 2 radius)] 
    (loop [p (new-rand-point sz)]
      (let [near (kd/nearest-neighbor tree p 2)]
        (cond
          (and (= 2 (count near))
               (< (:dist-squared (first near)) touch-dist-sq)
               (< (:dist-squared (second near)) touch-dist-sq))
          (recur (new-rand-point sz))

          (< (:dist-squared (first near)) touch-dist-sq)
          (vec p)
          ;[p (first near)]

          (> (:dist-squared (first near)) (* 5 touch-dist-sq))
          (recur (new-rand-point sz))

          :otherwise
          (recur (map + p (new-rand-point 2))))))))

(defn aggregate [nparts]
  (second
    (reduce (fn [[tree ps] _] 
              (let [p (stick-particle tree)]
                [(kd/insert tree p) (conj ps p)]))
            [(kd/build-tree [[0 0 0]]) [[0 0 0]]]
            (range nparts))))


;;;;;;;;;;;;;;; QUIL ;;;;;;;;;;;;;;;

(def points (aggregate 1000))
(defn draw [state]
  (q/background 50)
  ;(q/ambient-light 128 128 128)
  ;(q/point-light 200 50 50 -150 150 0)
  ;;(q/camera -200 0 0  0 0 0  0 1 0)
  (q/lights)
  (q/fill 150 100 150)
  (q/no-stroke)
  (q/with-translation [250 250 0]
  (doseq [p points]
    (q/with-translation p
      (q/sphere 5.))))
  ;(when (< (:frame state) 60)
  ;  (q/save-frame "dla-pull-####.png")
  )
(q/defsketch dla3d
  :draw draw
  :size [500 500]
  :renderer :p3d
  :middleware [m/fun-mode m/navigation-3d])
