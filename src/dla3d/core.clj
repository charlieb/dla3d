(ns dla3d.core
  (:require
    [thi.ng.geom.core :as g]
    [thi.ng.geom.vector :refer [vec3]]
    [kdtree :as kd]
    [thi.ng.geom.voxel.svo :as svo]
    [thi.ng.geom.voxel.isosurface :as iso]
    [thi.ng.geom.mesh.io :as mio]
    [clojure.java.io :as io]
    ))

(defn new-rand-point [xyz-range]
  [(- (rand (* 2 xyz-range)) xyz-range)
   (- (rand (* 2 xyz-range)) xyz-range)
   (- (rand (* 2 xyz-range)) xyz-range)])

;;;;;;;;;; KD-TREE Particle Approach ;;;;;;;;;;;;;;
(defn stick-particle [tree]
  (let [sz 25
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


(def res 0.25)
(defn voxelize-points [points]
  (let [scale (apply max (mapcat vec points))]
    (svo/apply-voxels svo/set-at (svo/voxeltree 32 res) points)))

(def iso-val 0.5)
(defn main [points]
  (with-open [o (io/output-stream "dla.stl")]
   (mio/write-stl
    (mio/wrapped-output-stream o)
    (g/tessellate (iso/surface-mesh (voxelize-points points) 10 iso-val)))))
