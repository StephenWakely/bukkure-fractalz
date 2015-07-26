(ns fractalz.pyramid
  (:require [bukkure.logging :as log]
            [bukkure.core :as core]
            [bukkure.items :as items]
            [bukkure.bukkit :as bukkit]))

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
  (let [top (+ size y -1)]
    [(-> col (+ x) (- (/ row 2)))
     (- top row)
     (-> depth (+ z) (- (/ row 2)))]))

(defn pyramid
  "Pulls rows from the sierpinsky triangle and transposes the positions into the players world."
  [player size]
  (let [{:keys [world blockX blockY blockZ]} (bean (.getLocation player))
        sierpinsky (sierpinsky3d size)]
    (log/info "Player %s %s %s" blockX blockY blockZ)
    (for [row (range 0 (dec size))
          col (range 0 (count (nth sierpinsky row)))
          depth (range 0 (count (nth sierpinsky row)))
          :let [b (-> sierpinsky (nth row) (nth col) (nth depth))]
          :when (odd? b)]
      (let [[x y z] (get-coords col row depth blockX blockY blockZ size)]
        [world x y z]))))

(defn set-blocks! [l] (doseq [block l]
                        (apply make-diamond-at block)))

(defn gen-pyramid [size player] (pyramid player size))

(defn digestable-blocks
  "Partitions the blocks into a chunk size that the minecraft renderer can handle."
  [l]
  (partition-all 500 l))

(defn do-pyramid
  [size player plugin]
  (let [pyramid (digestable-blocks (gen-pyramid size player))]
    (doseq [d pyramid]
      (bukkit/ui-sync plugin #(set-blocks! d)))))

(defn do-it-async
  [plugin blocks]
  (let [nthblock (atom 0)]
    (letfn [(do-it []
              (bukkit/delayed-task plugin
                              (fn []
                                (when (> (count blocks) @nthblock)
                                  (do
                                    (set-blocks! (nth blocks @nthblock))
                                    (swap! nthblock inc)
                                    (do-it))
                                  )) 20))]
      (do-it))))

(defn make-sierpinsky-pyramid
  [size player plugin]
  (let [blocks (digestable-blocks (gen-pyramid size player))]
    (do-it-async plugin blocks)))
