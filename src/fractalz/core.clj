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

(defn make-diamond-at
  [world x y z]
  (let [block (.getBlockAt world x y z)]
    (.setType block (:diamond_block items/materials))))


(defn add-mod2
  "Adds the numbers together and mods with 2 so the result is either 1 or 0"
  [& nums]
  (mod (apply + nums) 2))

(defn add-rows
  [[a b]]
  (map add-mod2 a b (next a) (next b)))

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

(defn sierpinsky3d
  [size]
  (take size (iterate next-level [[1]])))

(defn get-coords
 "Gets the coordinates to place the block at, ensuring the rows of the triangle are centralized"
 [col row depth x y z size]
 (let [top (+ size y)]
   [(-> col (+ x) (- (/ row 2)))
    (- top row)
    (-> depth (+ z) (- (/ row 2)))]))

(defn triangle3d
  "Pulls rows from the sierpinsky triangle and transposes the positions into the players world."
  [player size]
  (let [{:keys [world blockX blockY blockZ]} (bean (.getLocation player))
        sierpinsky (sierpinsky3d size)]
    (for [row (range 0 (dec size))
          col (range 0 (count (nth sierpinsky row)))
          depth (range 0 (count (nth sierpinsky row)))
          :let [b (-> sierpinsky (nth row) (nth col) (nth depth))]
          :when (odd? b)]
      (let [[x y z] (get-coords col row depth blockX blockY blockZ size)] 
        [world x y z]))))

(defn set-blocks! [l] (doseq [block l]
                        (apply make-diamond-at block)))

(defn gen-triangle [size player-name] (triangle3d (player-by-name player-name) size))

(defn digestable-blocks
  "Partitions the blocks into a chunk size that the minecraft renderer can handle."
  [l] (partition-all 500 l))

(defn make-triangle
  [size player-name]
  (map-indexed
   (fn [k v]
     (bukkure.bukkit/delayed-task @plugin
                                  #(set-blocks! v) (* k 20)))
   (digestable-blocks (gen-triangle size player-name))))


(defn on-enable [plugin-instance]
  (log/info "Starting your new bukkure plugin!")
  (reset! plugin plugin-instance))

(defn on-disable [plugin]
  (log/info "Stopping your new bukkure plugin!"))
