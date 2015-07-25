(ns fractalz.core
  (:require [bukkure.logging :as log]
            [bukkure.core :as core]
            [bukkure.items :as items]
            [bukkure.bukkit :as bukkit]))

(defonce plugin (atom nil))

(defn player-by-name
  "Returns the player given their name"
  [name]
  (let [player (filter #(= (.getName %) name) (bukkit/online-players))]
    (first player)))

(defn spawn-cow
  [player]
  (bukkit/ui-sync @plugin #(core/spawn-command player :cow)))

(defn make-blocks
  [player]
  (let [{:keys [x y z world]} (bean (.getLocation player))]
    (doseq [x' (range (- x 10) (+ x 10))
            z' (range (- z 10) (+ z 10))]
      (let [block (.getBlockAt world x' y z')]
        (.setType block (:diamond_block items/materials))))))

(defn make-blocks-sync
  [player]
  (bukkit/ui-sync @plugin #(make-blocks player)))

(defn next-row [row]
  (concat [1] (map +' row (drop 1 row)) [1]))

(defn pascals-triangle [n]
  (take n (iterate next-row '(1))))

(defn make-diamond-at
  [world x y z]
  (let [block (.getBlockAt world x y z)]
    (.setType block (:diamond_block items/materials))))

(defn triangle
  [player size fn]
  (let [{:keys [world x y z]} (bean (.getLocation player))
        pascal (pascals-triangle size)
        top (+ size y)]
    (doseq [row (range 0 (dec size))
            col (range 0 (count (nth pascal row)))]
      (let [b (-> pascal (nth row) (nth col))]
        (if (odd? b)
          (fn world (+ x col) (- top row) z))))))

(defn triangle-sync
  [player size]
  (bukkit/ui-sync @plugin #(triangle player size make-diamond-at)))


(comment
  1 = [1]

  2 = [[1 1]
       [1 1]]

  3 = [[1 2 1]
       [2 4 2]
       [1 2 1]]

  4 = [[1 3 3 1]
       [3 9 9 3]
       [3 9 9 3]
       [1 3 3 1]]

  5 = [[1 4  6  4  1]
       [4 16 24 16 4]
       [6 24 36 24 6]
       [4 16 24 16 4]
       [1 4  6  4  1]])

(defn add-rows
  [[a b]]
  (map +' a b (next a) (next b)))

(defn pad
  [previous]
  (let [len (-> previous count (+ 2))]
    `(~(repeat len 0)
      ~@(map (fn [a] `(0 ~@ a 0)) previous)
      ~(repeat len 0))))

(defn pairs
  "Takes each pair of rows and returns as a sequence"
  [[xs & rest]]
  (cons [xs (-> rest first)]
        (if (seq (next rest))
          (lazy-seq (pairs rest))
          [])))

(defn next-level
  [previous]
  (->> previous pad pairs (map add-rows)))

(defn pascal3d [size]
  (take size (iterate next-level [[1]])))

(defn triangle3d
  [player size fn]
  (let [{:keys [world x y z]} (bean (.getLocation player))
        pascal (pascal3d size)
        top (+ size y)]
    (doseq [row (range 0 (dec size))
            col (range 0 (count (nth pascal row)))
            depth (range 0 (count (nth pascal row)))]
      (let [b (-> pascal (nth row) (nth col) (nth depth))]
        (if (odd? b)
          (fn world (+ x col) (- top row) (+ z depth)))))))

(defn debug-params
  [w x y z]
  (println "X: " x " Y: " y " Z: " z)
)

(defn triangle3d-sync
  [player size]
  (bukkit/ui-sync @plugin #(triangle3d player size make-diamond-at)))


(defn on-enable [plugin-instance]
  (log/info "Starting your new bukkure plugin!")
  (reset! plugin plugin-instance))

(defn on-disable [plugin]
  (log/info "Stopping your new bukkure plugin!"))
