(ns dla3d.core
  (:require
    [clojure.string :refer [join]]
    [kdtree :as kd]
    ))

(defn new-rand-point [xyz-range]
  [(- (rand (* 2 xyz-range)) xyz-range)
   (- (rand (* 2 xyz-range)) xyz-range)
   (- (rand (* 2 xyz-range)) xyz-range)])

(defn stick-particle [tree size rad-fn]
  (loop [p (new-rand-point size)]
    (let [near (kd/nearest-neighbor tree p 2)]
      (cond
        (and (= 2 (count near))
             (< (:dist-squared (first near)) 
                (+ (Math/pow (rad-fn p) 2)
                   (Math/pow (rad-fn (:point (first near))) 2)))
             (< (:dist-squared (second near))
                (+ (Math/pow (rad-fn p) 2)
                   (Math/pow (rad-fn (:point (second near))) 2))))
        (recur (new-rand-point size))

        (< (:dist-squared (first near)) 
           (+ (Math/pow (rad-fn p) 2)
              (Math/pow (rad-fn (:point (first near))) 2)))
        ;(vec p)
        {:p (vec p) :hit (:point (first near))}

        (> (:dist-squared (first near)) 
           (* 5 (+ (Math/pow (rad-fn p) 2)
                   (Math/pow (rad-fn (:point (first near))) 2))))
        (recur (new-rand-point size))

        :otherwise
        (recur (map + p (new-rand-point 0.5)))))))

(defn- sphere-shell [p] 
  (if (< (Math/abs (- 50 (apply + (map * p p)))) 10)
    0.1
    1.))

(defn- max-dist [p maxd]
  (let [margin 10
        margin-sq 100]
    (if (> (+ margin-sq (apply + (map * p p)))
           (* maxd maxd))
      (+ margin (Math/sqrt (apply + (map * p p))))
      maxd)))


(defn aggregate [nparts]
  (let [rad-fn sphere-shell ;(constantly 1.)
        max-rand-point-fn max-dist
        ]
    (loop [tree (kd/build-tree [[0 0 0]]) 
           points []
           maxd 10
           i nparts]
      (if (zero? i)
        points
        (let [p (stick-particle tree maxd rad-fn)]
          (println i maxd p)
          (recur (kd/insert tree (:p p))
                 (conj points p)
                 (max-rand-point-fn (:p p) maxd)
                 (dec i)))))))


;;;;;;;;;;;; BLENDER PYTHON ;;;;;;;;;;;;
(defn vec-to-python-string [v] (join ["[" (join "," v) "]"]))
(defn to-python [filename points]
  (spit filename
    (str "["
         (join ","
               (map (fn [p] (str "["
                                 (vec-to-python-string (:p p))
                                 ","
                                 (vec-to-python-string (:hit p)) 
                                 "]"))
                    points))
         "]")))
