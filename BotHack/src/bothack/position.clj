(ns bothack.position
  (:require [clojure.tools.logging :as log]
            [clojure.string :as string]
            [bothack.util :refer :all]))

(defrecord Position [x y]
  bothack.bot.IPosition
  (x [pos] (:x pos))
  (y [pos] (:y pos)))

(defmethod print-method Position [pos w]
  (.write w (str "#Position{:x " (:x pos) ", :y " (:y pos) "}")))

(defn position-map
  "When an actual map is desired (position records are not completely equal to
  {:x X :y Y})"
  [of]
  (select-keys of [:x :y]))

(defn position
  ([x y] (Position. x y))
  ([of] {:post [(:x %) (:y %)]} (->Position (:x of) (:y of))))

(defn valid-position?
  ([x y] (and (<= 0 x 79) (<= 1 y 21)))
  ([{:keys [x y]}] (valid-position? x y)))

(defn at
  "Tile of the level at given terminal position"
  ([level x y]
   {:pre [(valid-position? x y)]}
   (get-in level [:tiles (dec y) x]))
  ([level {:keys [x y] :as pos}] (at level x y)))

(def directions [:NW :N :NE :W :E :SW :S :SE])
(def opposite {:NW :SE :N :S :NE :SW :W :E :E :W :SW :NE :S :N :SE :NW})

(def deltas [[-1 -1] [0 -1] [1 -1] [-1 0] [1 0] [-1 1] [0 1] [1 1]])
(def dirmap (merge (zipmap directions deltas)
                   (zipmap deltas directions)))

(def straight #{:N :W :S :E})
(def diagonal #{:NW :SW :NE :SE})

(defn adjacent? [pos1 pos2]
  (and (<= (Math/abs ^long (unchecked-subtract (:x pos1) (:x pos2))) 1)
       (<= (Math/abs ^long (unchecked-subtract (:y pos1) (:y pos2))) 1)))

(defn towards [from to]
  (get dirmap [(Long/compare (:x to) (:x from))
               (Long/compare (:y to) (:y from))]))

(defn diagonal? [from to]
  (diagonal (towards from to)))

(defn straight? [from to]
  (straight (towards from to)))

(defn neighbors
  ([level tile]
   {:pre [(:tiles level)]}
   (map #(at level %) (neighbors tile)))
  ([pos]
   (for [d deltas
         :let [nbr (position (unchecked-add (:x pos) (d 0))
                             (unchecked-add (:y pos) (d 1)))]
         :when (valid-position? nbr)]
     nbr)))

(defn including-origin
  "Include origin position in given *neighbors function results"
  ([nbr-fn pos]
   (conj (nbr-fn pos) (position pos)))
  ([nbr-fn level pos]
   (conj (nbr-fn level pos) (at level pos))))

(defn straight-neighbors
  ([tile]
   (filter (partial straight? tile) (neighbors tile)))
  ([level tile]
   (filter (partial straight? tile) (neighbors level tile))))

(defn diagonal-neighbors
  ([tile]
   (filter (partial diagonal? tile) (neighbors tile)))
  ([level tile]
   (filter (partial diagonal? tile) (neighbors level tile))))

(defn in-direction
  ([level from dir]
   (some->> (in-direction from dir) (at level)))
  ([from dir]
   {:pre [(valid-position? from) (some? dir)]}
   (let [dir (enum->kw dir)
         res (position (unchecked-add ((dirmap dir) 0) (:x from))
                       (unchecked-add ((dirmap dir) 1) (:y from)))]
     (if (valid-position? res)
       res))))

(defn distance [from to]
  (max (Math/abs ^long (unchecked-subtract (:x from) (:x to)))
       (Math/abs ^long (unchecked-subtract (:y from) (:y to)))))

(defn distance-manhattan [from to]
  (unchecked-add (Math/abs ^long (unchecked-subtract (:x from) (:x to)))
                 (Math/abs ^long (unchecked-subtract (:y from) (:y to)))))

(defn rectangle [NW-corner SE-corner]
  (for [x (range (:x NW-corner) (inc (:x SE-corner)))
        y (range (:y NW-corner) (inc (:y SE-corner)))]
    (position x y)))

(defn rectangle-boundary [NW-corner SE-corner]
  (for [x (range (:x NW-corner) (inc (:x SE-corner)))
        y (range (:y NW-corner) (inc (:y SE-corner)))
        :when (or (= x (:x NW-corner)) (= x (:x SE-corner))
                  (= y (:y NW-corner)) (= y (:y SE-corner)))]
    (position x y)))

(defn in-line [from to]
  (or (= (:x from) (:x to))
      (= (:y from) (:y to))))

(defn to-position
  "Sequence of keys to move the cursor from the corner to the given position"
  [pos]
  {:pre (valid-position? pos)}
  (string/join (concat (repeat 10 \H) (repeat 4 \K) ; to corner
                       (repeat (dec (:y pos)) \j) (repeat (:x pos) \l))))
