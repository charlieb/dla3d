(ns dla3d.core
  (:require
    [clojure.string :refer [join]]
    [kdtree :as kd]
    ))

(defn new-rand-point [xyz-range]
  [(- (rand (* 2 xyz-range)) xyz-range)
   (- (rand (* 2 xyz-range)) xyz-range)
   (- (rand (* 2 xyz-range)) xyz-range)])

(defn stick-particle [tree size rad-fn allow-fn]
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

        (and
          (< (:dist-squared (first near)) 
             (+ (Math/pow (rad-fn p) 2)
                (Math/pow (rad-fn (:point (first near))) 2)))
          (allow-fn p))
        ;(vec p)
        {:p (vec p) :hit (:point (first near))}

        (> (:dist-squared (first near)) 
           (* 5 (+ (Math/pow (rad-fn p) 2)
                   (Math/pow (rad-fn (:point (first near))) 2))))
        (recur (new-rand-point size))

        :otherwise
        (recur (map + p (new-rand-point 0.5)))))))

(defn- in-sphere-shell? [p] 
  (< (Math/abs (- 100 (apply + (map * p p)))) 10))

(defn- sphere-shell [p] 
  (if (in-sphere-shell? p) 0.1 1.))

(defn- max-dist [p maxd]
  (let [margin 10
        margin-sq 100]
    (if (> (+ margin-sq (apply + (map * p p)))
           (* maxd maxd))
      (+ margin (Math/sqrt (apply + (map * p p))))
      maxd)))


(defn aggregate [nparts]
  (let [max-rand-point-fn max-dist
        rad-fn (constantly 0.5) ;sphere-shell ;
        allow-point in-sphere-shell? ;(constantly true)
        initial-points [[10 0 0] [0 10 0] [0 0 10]
                        [-10 0 0] [0 -10 0] [0 0 -10]] ; [[0 0 0]
        ]
    (loop [tree (kd/build-tree initial-points) 
           points []
           maxd 20
           i nparts]
      (if (zero? i)
        points
        (let [p (stick-particle tree maxd rad-fn allow-point)]
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
