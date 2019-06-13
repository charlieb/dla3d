(ns dla3d.core
  (:require
    [thi.ng.geom.core :as g]
    [thi.ng.geom.vector :refer [vec3]]
    [thi.ng.geom.spatialtree :as st]
    [thi.ng.geom.voxel.svo :as svo]
    [thi.ng.geom.voxel.isosurface :as iso]
    [thi.ng.geom.mesh.io :as mio]
    [clojure.java.io :as io]
    ))

(defn new-rand-point [xyz-range]
  (vec3 (- (rand xyz-range) (/ xyz-range 2))
        (- (rand xyz-range) (/ xyz-range 2))
        (- (rand xyz-range) (/ xyz-range 2))))
(defn dla-points
  "I don't do a whole lot."
  [npoints start-range]
  (let [tree (st/octree 0 0 0 100 100 100)]
    (g/add-point tree (vec3 0 0 0) (vec3 0 0 0))
    (loop [tree tree
           i npoints
           p (new-rand-point start-range)]
      (println i p)
      (cond (zero? i)
            (st/select-with-sphere tree (vec3 0 0 0) 50)

            (st/points-in-sphere? tree p 5) 
            (recur (g/add-point tree p p)
                   (dec i)
                   (new-rand-point start-range))

            (not (st/points-in-sphere? tree p 10))
            (recur tree i (new-rand-point start-range))

            :otherwise 
            (recur tree i (.+ p (new-rand-point 1.)))))))


;(defn voxelize-points [points]
;  (let [scale (apply max (mapcat vec points))])
;  (svo/apply-voxels svo/set-at (svo/voxeltree 32 res) points))
;
;(defn main []
;  (with-open [o (io/output-stream "dla.stl")]
;   (mio/write-stl
;    (mio/wrapped-output-stream o)
;    (g/tessellate (iso/surface-mesh v 10 iso-val)))))
