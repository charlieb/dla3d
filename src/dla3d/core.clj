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
  (let [sz 50
        radius 1.0
        touch-dist-sq (* 2 radius 2 radius)] 
    (loop [p (new-rand-point sz)]
      (let [near (kd/nearest-neighbor tree p 2)]
        (cond
          (and (= 2 (count near))
               (< (:dist-squared (first near)) touch-dist-sq)
               (< (:dist-squared (second near)) touch-dist-sq))
          (recur (new-rand-point sz))

          (< (:dist-squared (first near)) touch-dist-sq)
          ;(vec p)
          {:p (vec p) :hit (:point (first near))}

          (> (:dist-squared (first near)) (* 5 touch-dist-sq))
          (recur (new-rand-point sz))

          :otherwise
          (recur (map + p (new-rand-point (* radius 0.75 )))))))))

(defn aggregate [nparts]
  (second
    (reduce (fn [[tree ps] i] 
              (let [p (stick-particle tree)]
                (println i p)
                [(kd/insert tree (:p p)) (conj ps p)]))
            [(kd/build-tree [[0 0 0]]) [{:p [0 0 0] :hit [0 0 0]}]]
            (range nparts))))


;;;;;;;;;;;;;;; QUIL ;;;;;;;;;;;;;;;

(defn update-state [state]
  (if (:points state)
    state
    (assoc state
           :points (aggregate 1000)
           ;position [-20 0 0]
           )))

(defn draw [state]
  (q/background 0)
  (q/ambient-light 80 80 80)
  (q/point-light 200 50 50 -150 150 0)
  ;(q/camera -20 0 0  0 0 0  0 1 0)
  ;(q/lights)
  (q/with-translation [250 250 0]
    (doseq [p (:points state)]
      (q/stroke 200 150 200)
      (q/stroke-weight 1)
      ;(print (concat (:p p) (:hit p)))
      (q/line (:p p) (:hit p))
      ;(apply q/line (concat (:p p) (:hit p)))
;      (q/fill 150 100 150)
;      (q/no-stroke)
;      (q/with-translation (:p p)
;        (q/sphere 0.1))
        ))
  ;(when (< (:frame state) 60)
  ;  (q/save-frame "dla-pull-####.png")
  )

(q/defsketch dla3d
  :draw draw
  :update update-state
  :size [500 500]
  :renderer :p3d
  :middleware [m/fun-mode m/navigation-3d])
