(ns bothack.dungeon
  (:require [clojure.tools.logging :as log]
            [clojure.string :as string]
            [clojure.set :refer [intersection]]
            [bothack.monster :refer :all]
            [bothack.util :refer :all]
            [bothack.level :refer :all]
            [bothack.tile :refer :all]
            [bothack.position :refer :all]
            [bothack.delegator :refer :all]))

(defn- next-branch-id [game]
  (keyword (str "unknown-" ((fnil inc 0) (:last-branch-no game)))))

(def branches
  #{:main :mines :sokoban :quest :ludios :vlad :wiztower :earth :fire :air
    :water :astral})

(def subbranches
  #{:mines :sokoban :ludios :vlad :quest :wiztower :earth :air :fire :water
    :astral})

(def upwards-branches #{:sokoban :vlad :wiztower})

(def portal-branches #{:quest :wiztower :ludios})

(def planes #{:earth :air :fire :water :astral})

(defn upwards? [branch] (upwards-branches branch))

(defn dlvl-number [dlvl] (parse-int (first (re-seq #"\d+" dlvl))))

(defn dlvl
  "Dlvl number or -1 for non-numbered branches"
  [game-or-level]
  (or (dlvl-number (:dlvl game-or-level)) -1))

(defn dlvl-compare
  "Only makes sense for dlvls within one branch."
  ([branch d1 d2]
   (if (upwards? branch)
     (dlvl-compare d2 d1)
     (dlvl-compare d1 d2)))
  ([d1 d2]
   (if (every? #(.contains ^String % ":") [d1 d2])
     (compare (dlvl-number d1) (dlvl-number d2))
     (compare d1 d2))))

(defn ensure-branch [game branch-id]
  (log/debug "ensuring branch" branch-id)
  (update-in game [:dungeon :levels branch-id]
             #(or % (sorted-map-by (partial dlvl-compare branch-id)))))

(defn add-level [{:keys [dungeon] :as game} {:keys [branch-id] :as level}]
  (assoc-in (ensure-branch game branch-id)
            [:dungeon :levels branch-id (:dlvl level)]
            level))

(defn change-dlvl
  "Apply function to dlvl number if there is one, otherwise no change.  The
  result dlvl may not actually exist."
  [f dlvl]
  (if-let [n (dlvl-number dlvl)]
    (string/replace dlvl #"\d+" (str (f n)))
    dlvl))

(defn prev-dlvl
  "Dlvl closer to branch entry (for dlvl within the branch), no change for
  unnumbered dlvls.  Single arg variant assumes :main branch."
  ([dlvl] (prev-dlvl :main dlvl))
  ([branch dlvl]
   (if (upwards? branch)
     (change-dlvl inc dlvl)
     (change-dlvl dec dlvl))))

(defn next-dlvl
  "Dlvl further from branch entry (for dlvl within the branch), no change for
  unnumbered dlvls.  Single arg variant assumes :main branch."
  ([dlvl] (next-dlvl :main dlvl))
  ([branch dlvl]
   (if (upwards? branch)
     (change-dlvl dec dlvl)
     (change-dlvl inc dlvl))))

(defn branch-key
  ([{:keys [branch-id] :as game}]
   {:pre [branch-id]}
   (branch-key game branch-id))
  ([{:keys [dungeon] :as game} level-or-branch-id]
   {:pre [dungeon]}
   (let [branch-id (if (keyword? level-or-branch-id)
                     level-or-branch-id
                     (:branch-id level-or-branch-id))]
     (get (:id->branch dungeon) branch-id branch-id))))

(defn curlvl [game]
  {:pre [(:dungeon game)]}
  (-> game :dungeon :levels (get (branch-key game)) (get (:dlvl game))))

(defn curlvl-monsters [game]
  {:pre [(:dungeon game)]}
  (-> game curlvl :monsters vals))

(defn update-curlvl
  "Update the current Level by applying update-fn to its current value and args"
  [game update-fn & args]
  {:pre [(:dungeon game)]}
  (apply update-in game [:dungeon :levels (branch-key game) (:dlvl game)]
         update-fn args))

(defn add-curlvl-tag [game & tags]
  {:pre [(:dungeon game)]}
  (log/debug "tagging curlvl with" tags)
  (apply update-curlvl game update :tags conj tags))

(defn curlvl-tags [game]
  {:pre [(:dungeon game)]}
  (-> game curlvl :tags))

(defn remove-monster
  "Removes a monster from the given position"
  [game pos]
  {:pre [(:dungeon game)]}
  (update-curlvl game update :monsters dissoc (position pos)))

(defn reset-monster
  "Sets the monster at the given position to the new value"
  [game-or-level monster]
  (if (:dungeon game-or-level)
    (update-curlvl game-or-level reset-monster monster)
    (assoc-in game-or-level [:monsters (position monster)] monster)))

(defn update-monster
  "Update the monster on current level at given position (if there is one) by
  applying update-fn to its current value and args."
  [game pos update-fn & args]
  (if ((:monsters (curlvl game)) (position pos))
    (apply update-curlvl game update-in [:monsters (position pos)]
           update-fn args)
    game))

(defn monster-at
  ([game-or-level x y] (monster-at game-or-level (position x y)))
  ([game-or-level pos]
   (if pos
     (if (:monsters game-or-level)
       (get-in game-or-level [:monsters (position pos)])
       (monster-at (curlvl game-or-level) pos)))))

(defn real-boulder? [level pos]
  (and (boulder? (at level pos)) (not (mimic? (monster-at level pos)))))

(defn update-at
  "Update the Tile on current or given level at given position by applying
  update-fn to its current value and args"
  [game-or-level pos update-fn & args]
  (if (:dungeon game-or-level)
    (apply update-curlvl game-or-level update-at pos update-fn args)
    (apply update-in game-or-level [:tiles (dec (:y pos)) (:x pos)]
           update-fn args)))

(defn update-at-player
  "Update the Tile at player's position by applying update-fn to its current
  value and args"
  [game update-fn & args]
  {:pre [(:dungeon game)]}
  (apply update-at game (:player game) update-fn args))

(defn update-from-player
  "Update the Tile one move from player's position in given direction by
  applying update-fn to its current value and args"
  [game dir update-fn & args]
  {:pre [(:dungeon game)]}
  (apply update-at game (in-direction (:player game) dir) update-fn args))

(defn update-item-at-player [game idx update-fn & args]
  (apply update-at-player game update-in [:items idx] update-fn args))

(defn update-around
  "Update the Tiles around (not including) given position by applying update-fn
  to their current value and args"
  [game pos update-fn & args]
  {:pre [(:dungeon game)]}
  (reduce #(apply update-at %1 %2 update-fn args)
          game
          (neighbors pos)))

(defn update-around-player
  "Update the Tiles around player's position by applying update-fn to their
  current value and args"
  [game update-fn & args]
  (apply update-around game (:player game) update-fn args))

(defn at-curlvl
  "Returns the Tile at the given position on the current level"
  ([game x y] (at-curlvl game {:x x :y y}))
  ([game pos]
   {:pre [(:dungeon game)]}
   (at (curlvl game) pos)))

(defn at-player
  "Returns the Tile at the player's position"
  [game]
  {:pre [(:dungeon game)]}
  (at-curlvl game (:player game)))

(defn get-branch
  "Returns {Dlvl => Level} map for branch-id (or current branch)"
  ([game]
   (get-branch game (branch-key game)))
  ([game branch-id]
   (get-in game [:dungeon :levels (branch-key game branch-id)])))

(defn get-level
  "Return Level in the given branch with the given tag or dlvl, if such was
  visited already"
  [game branch dlvl-or-tag]
  {:pre [(:dungeon game)]}
  (if-let [levels (get-branch game branch)]
    (if (keyword? dlvl-or-tag)
      (some #(and ((:tags %) dlvl-or-tag) %) (vals levels))
      (levels dlvl-or-tag))))

(defn get-dlvl [game branch dlvl-or-tag]
  (:dlvl (get-level game branch dlvl-or-tag)))

(defn lit?
  "Actual lit-ness is hard to determine and not that important, this is a
  pessimistic guess."
  [player level pos]
  {:pre [(:hp player)]}
  (let [tile (at level pos)]
    (or (adjacent? pos player) ; TODO actual player light radius
        (= \. (:glyph tile))
        (and (= \# (:glyph tile)) (= :white (:color tile))))))

(defn map-tiles
  "Call f on each tile (or each tuple of tiles if there are more args) in 21x80
  vector structures to again produce 21x80 vector of vectors"
  [f & tile-colls]
  (apply (partial mapv (fn [& rows]
                         (apply (partial mapv #(apply f %&)) rows)))
         tile-colls))

(def ^:private main-features ; these don't appear in the mines (except for end and minetown)
  #{:door-closed :door-open :door-locked :door-secret :altar :sink :fountain :throne})

(defn- has-features? [level]
  "checks for features not occuring in the mines (except town/end)"
  {:pre [(:tiles level)]}
  (some (comp main-features :feature) (tile-seq level)))

(defn- same-glyph-diag-walls
  " the check we make is that any level where there are diagonally adjacent
  walls with the same glyph, it's mines. that captures the following:
  .....
  ..---
  ..-..
  .....
  thanks TAEB!"
  [level]
  (some (fn has-same-glyph-diag-neighbor? [tile]
          (->> (neighbors level tile)
               (remove #(straight (towards tile %)))
               (some #(and (every? wall? [tile %])
                           (= (:glyph tile) (:glyph %))))))
        (apply concat (take-nth 2 (:tiles level)))))

(def soko1-14 "                                |..^^^^8888...|")
(def soko2-12 "                                |..^^^<|.....|")

(defn- in-soko? [game]
  (and (<= 5 (dlvl game) 9)
       (or (.startsWith (get-in game [:frame :lines 14]) soko1-14)
           (.startsWith (get-in game [:frame :lines 12]) soko2-12))))

(defn- recognize-branch [game level]
  (cond (in-soko? game) :sokoban
        (has-features? level) :main
        (same-glyph-diag-walls level) :mines))

(defn branch-entry
  "Return Dlvl of :main containing entrance to branch, if static or already visited"
  [game branch]
  {:pre [(:dungeon game)]}
  (if (planes branch)
    "Dlvl:1"
    (if-let [l (get-level game :main (branch-key game branch))]
      (:dlvl l))))

(defn- merge-tile [new-tile old-tile]
  (-> (if (:branch-id old-tile)
        (assoc new-tile :branch-id (:branch-id old-tile))
        new-tile)
      (update :tags into (:tags old-tile))
      (update :first-walked max* (:first-walked old-tile))
      (update :walked max* (:walked old-tile))
      (update :seen #(or % (:seen old-tile)))
      (update :feature #(or % (:feature old-tile)))
      (update :searched + (:searched old-tile))))

(defn- merge-levels [old-level new-level]
  (log/debug "merging dlvl" (:dlvl old-level))
  (-> new-level
      (update :blueprint #(or (:blueprint old-level) %))
      ; forget monsters
      (update :tags into (:tags old-level))
      (update :tiles (partial map-tiles merge-tile) (:tiles old-level))))

(defn merge-branch-id
  "When a branch identity is determined, associate the temporary ID to its real
  ID (returned by branch-key)"
  [{:keys [dungeon] :as game} branch-id branch]
  {:pre [dungeon]}
  (log/debug "merging branch-id" branch-id "to branch" branch)
  ;(log/debug dungeon)
  (-> game
      (assoc-in [:dungeon :id->branch branch-id] branch)
      (ensure-branch branch)
      (update-in [:dungeon :levels branch] (partial merge-with merge-levels)
                 (-> dungeon :levels branch-id))
      (update-in [:dungeon :levels :main (branch-entry game branch-id) :tags]
                 #(-> % (disj branch-id) (conj branch)))
      (update-in [:dungeon :levels] dissoc branch-id)))

(defn infer-branch [game]
  {:pre [(:dungeon game)]}
  (if (branches (branch-key game))
    game ; branch already known
    (let [level (curlvl game)]
      (if-let [branch (recognize-branch game level)]
        (merge-branch-id game (:branch-id level) branch)
        game)))) ; failed to recognize

(defn in-maze-corridor? [level pos]
  (->> (neighbors level pos) (filter wall?) (more-than? 5)))

(def soko2a-16 "                          |...|..8-.8.^^^^^^^^^^^^.|")
(def soko2b-16 "                        |....|..8.8.^^^^^^^^^^^^^^^.|")
(def soko3a-12 "                              |....-8--8-|...<...|")
(def soko3b-9 "                              |..|.8.8.|88.|.....|")
(def soko4a-18 "                          |..8.....|     |-|.....|--") ; BoH variant
(def soko4b-5 "                            |..^^^^^^^^^^^^^^^^^^..|") ; "oR variant

(def soko-recog ; [y rtrimmed-line :tag]
  [[14 soko1-14 :soko-1b]
   [12 soko2-12 :soko-1a]
   [16 soko2a-16 :soko-2a]
   [16 soko2b-16 :soko-2b]
   [12 soko3a-12 :soko-3a]
   [9 soko3b-9 :soko-3b]
   [18 soko4a-18 :soko-4a]
   [5 soko4b-5 :soko-4b]])

(defn- recognize-soko [game]
  (or (some (fn [[y line tag]]
              (if (.startsWith (get-in game [:frame :lines y]) line)
                tag))
            soko-recog)
      (throw (IllegalStateException. "unrecognized sokoban level!"))))

(def fake-wiztower-water
  [{:y 9 :x 35} {:y 9 :x 36} {:y 9 :x 37} {:y 9 :x 38} {:y 9 :x 39} {:y 9 :x 40}
   {:y 9 :x 41} {:y 10 :x 35} {:y 10 :x 36} {:y 10 :x 40} {:y 10 :x 41}
   {:y 11 :x 35} {:y 11 :x 41} {:y 12 :x 35} {:y 12 :x 41} {:y 13 :x 35}
   {:y 13 :x 41} {:y 14 :x 35} {:y 14 :x 36} {:y 14 :x 40} {:y 14 :x 41}
   {:y 15 :x 35} {:y 15 :x 36} {:y 15 :x 37} {:y 15 :x 38} {:y 15 :x 39}
   {:y 15 :x 40} {:y 15 :x 41}])

(def fake-wiztower-portal {:x 38 :y 12})

(defn infer-tags [game]
  (let [level (curlvl game)
        curdlvl (dlvl level)
        tags (:tags level)
        branch (branch-key game)
        has-features? (has-features? level)
        at-level (partial at level)]
    (cond-> game
      (and (= :main branch) (<= 21 curdlvl 28)
           (not (tags :medusa-1)) (not (tags :medusa-2))
           (some floor? (for [y (range 2 21)]
                          (at level 3 y)))
           (every? #(or (pool? (at level 2 %))
                        (monster-at level (position 2 %)))
                   (range 2 21))) (add-curlvl-tag :medusa :medusa-1)
      (and (= :main branch) (<= 21 curdlvl 28)
           (not (tags :medusa-1)) (not (tags :medusa-2))
           (not-any? floor? (for [y (range 2 21)]
                              (at level 3 y)))
           (every? pool? [(at level 7 15) (at level 7 16) (at level 7 17)])
           (wall? (at level 8 15))
           (wall? (at level 8 17))) (add-curlvl-tag :medusa :medusa-2)
      (and (= :main branch) (<= 21 curdlvl 28) (not (tags :medusa))
           (= (:dlvl (:last-state game)) (:dlvl game))
           (or (some medusa? (curlvl-monsters (:last-state game)))
               (every? #(or (pool? (at level 2 %))
                            (monster-at level (position 2 %)))
                       (range 3 15)))) (add-curlvl-tag :medusa)
      (and (= :main branch) (<= 25 curdlvl 29)
           (not (tags :castle))
           (or (drawbridge? (at level 14 12))
               (and (every? pool? (for [x (range 8 16)]
                                     (at level x 20)))
                    (wall? (at level 7 20)))
               (and (every? pool? (for [x (range 8 16)]
                                     (at level x 4)))
                    (wall? (at level 7 4))))) (add-curlvl-tag :castle)
      (and (= :sokoban branch)
           (not (some #{:soko-1a :soko-1b :soko-2a :soko-2b
                        :soko-3a :soko-3b :soko-4a :soko-4b}
                      (:tags level)))) ((fn sokotag [game]
                                          (let [tag (recognize-soko game)
                                                res (add-curlvl-tag game tag)]
                                            (if (#{:soko-4a :soko-4b} tag)
                                              (add-curlvl-tag res :end)
                                              res))))
      (and (= :main branch) (<= 10 curdlvl 12)
           (not (tags :bigroom))
           (some (fn lots-floors? [row]
                   (->> (for [x (range 3 78)] (at level x row))
                        (take-while (complement corridor?))
                        (filter #(or (floor? %) (monster-at level %)))
                        (more-than? 45)))
                 [8 16])) (add-curlvl-tag :bigroom)
      (and (= :main branch) (<= 5 curdlvl 9)
           (= (:dlvl (:last-state game)) (:dlvl game))
           (some oracle?
                 (curlvl-monsters (:last-state game)))) (add-curlvl-tag :oracle)
      (and (= :main branch) (<= 36 curdlvl 47) (not (tags :wiztower-level))
           (or (some :undiggable (map at-level wiztower-inner-boundary))
               (and (not-any? floor? (map at-level wiztower-inner-boundary))
                    (->> (map at-level wiztower-boundary)
                         (filter (some-fn wall? :dug))
                         (more-than? 20))
                    (->> (map at-level wiztower-boundary)
                         (filter (every-pred floor? (complement :dug)))
                         (less-than? 5))))) (add-curlvl-tag :wiztower-level)
      (and (<= 5 curdlvl 9) (= :mines branch) (not (tags :minetown))
           has-features?) (add-curlvl-tag :minetown)
      (and (<= 5 curdlvl 9) (stairs-up? (at level 3 2)) (tags :minetown)
           (not (tags :minetown-grotto))) (add-curlvl-tag :minetown-grotto)
      (and (<= 27 curdlvl 36) (not (:asmodeus (:tags level)))
           (or (and (some :undiggable (tile-seq level))
                    (stairs-down? (at level 27 13)))
               (every? #(= \- (:glyph %)) [(at level (position 66 10))
                                           (at level (position 66 9))])
               (every? #(= \- (:glyph %)) [(at level (position 66 14))
                                           (at level (position 66 15))])
               (door? (at level 66 12)))) (add-curlvl-tag :asmodeus)
      (and (<= 29 curdlvl 36) (not (:juiblex (:tags level)))
           (->> (tile-seq level)
                (filter pool?)
                (more-than? 24))) (add-curlvl-tag :juiblex)
      (and (<= 31 curdlvl 38) (not (tags :baalzebub))
           (or (and (not-any? (fn [[x y]]
                                (wall? (at level x y)))
                              [[31 11] [32 11] [33 11] [34 11]
                               [31 13] [32 13] [33 13] [34 13]])
                    (not-any? (fn [[x y]]
                                (let [tile (at level x y)]
                                  (or (not (wall? tile)) (dug? tile))))
                              [[30 10] [35 10] [30 11] [35 11]
                               [30 13] [35 13] [30 14] [35 14]]))
               (and (stairs-down? (at level 72 12))
                    (door? (at level 70 12))))) (add-curlvl-tag :baalzebub)
      (and (= branch :main) (<= 40 curdlvl 51) (not (:fake-wiztower tags))
           (->> fake-wiztower-water (map at-level)
                (some pool?))) (add-curlvl-tag :fake-wiztower)
      (and (= branch :main) (<= 40 curdlvl) (not (:sanctum tags))
           (= (some-> game (get-level :main :end) dlvl inc)
              curdlvl)) (add-curlvl-tag :sanctum)
      (and (#{:wiztower :vlad} branch)
           (not-any? #{:bottom :middle :end}
                     tags)) (add-curlvl-tag (get {0 :bottom
                                                  1 :middle
                                                  2 :end}
                                                 (-> (get-branch game branch)
                                                     keys first dlvl-number
                                                     (- curdlvl))))
      (and (<= 10 curdlvl 13) (= :mines branch)
           (not-any? tags #{:minesend-1 :minesend-2 :minesend-3})
           (and (stairs-up? (at level 38 8))
                (or (and (every? floor? (for [x (range 35 42)]
                                     (at level (position x 7))))
                         (every? wall? (for [x (range 35 42)]
                                         (at level (position x 6)))))
                    (->> (tile-seq level) (filter :undiggable)
                         (more-than? 2))))) (add-curlvl-tag :minesend-1)
      (and (<= 10 curdlvl 13) (= :mines branch) (not (tags :end))
           has-features?) (add-curlvl-tag :end))))

(defn next-plane
  "Next unvisited elemental plane"
  [game]
  (condp #(get-branch %2 %1) game
    :fire :water
    :air :fire
    :earth :air))

(defn initial-branch-id
  "Choose branch-id for a new dlvl reached by stairs."
  [game dlvl]
  ; TODO could check for already found parallel branches and disambiguate, also check if going up or down and disambiguate soko/mines
  (or (if (= "End Game" dlvl) :earth)
      (subbranches (branch-key game))
      (if-not (<= 3 (dlvl-number dlvl) 9) :main)
      (next-branch-id game)))

(defn dlvl-range
  "Only works for :main and :mines"
  ([branch]
   (dlvl-range branch "Dlvl:1"))
  ([branch start]
   (dlvl-range branch start 60))
  ([branch start howmany]
   (for [x (range howmany)]
     (change-dlvl #(+ % x) start))))

(defn dlvl-from-entrance [game branch in-branch-depth]
  (some->> (get-branch game branch) keys first
           (change-dlvl #(+ % (dec in-branch-depth)))))

(defn dlvl-from-tag [game branch tag after-tag-depth]
  (some->> (get-dlvl game branch tag)
           (change-dlvl #(+ % (dec after-tag-depth)))))

(defn ensure-curlvl
  "If current branch-id + dlvl has no level associated, create a new empty level"
  [{:keys [dlvl] :as game}]
  ;(log/debug "ensuring curlvl:" dlvl "- branch:" (branch-key game))
  (if-not (get-in game [:dungeon :levels (branch-key game) dlvl])
    (add-level game (new-level dlvl (branch-key game)))
    game))

(defn- shopkeeper-look? [game tile-or-monster]
  (and (not= (position (:player game)) (position tile-or-monster))
       (= \@ (:glyph tile-or-monster))
       (= :white (:color tile-or-monster))))

(defn- room-rectangle [game NW-corner SE-corner kind]
  (log/debug "room rectangle:" NW-corner SE-corner kind)
  (when (< 20 (max (- (:x SE-corner) (:x NW-corner))
                   (- (:y SE-corner) (:y NW-corner))))
    (log/error "spilled room at" (:dlvl game) (branch-key game)))
  (as-> game res
    (reduce #(update-at %1 %2 assoc :room kind)
            res
            (rectangle NW-corner SE-corner))
    (if (shops kind)
      (reduce #(update-at %1 %2 assoc :feature :wall)
              res
              (filter (comp unknown? (partial at-curlvl game))
                      (rectangle-boundary NW-corner SE-corner)))
      res)))

(defn- boundary? [tile]
  (or (wall? tile) (door? tile) (:dug tile)))

(defn- missing-wall? [level tile]
  (if-not (boundary? tile)
    (let [nbrs (neighbors level tile)
          vert (filter #(= (:x %) (:x tile)) nbrs)
          horiz (filter #(= (:y %) (:y tile)) nbrs)]
      (or (and (less-than? 3 (filter boundary? nbrs))
               (some boundary? vert)
               (some boundary? horiz))
          (every? boundary? vert)
          (every? boundary? horiz)))))

(defn- floodfill-room [game pos kind]
  (log/debug "room floodfill from:" pos "type:" kind)
  (let [level (curlvl game)
        origin (at level pos)]
    (loop [closed #{}
           NW-corner origin
           SE-corner origin
           open (if (shopkeeper-look? game origin)
                  (set (including-origin neighbors level origin))
                  #{origin})]
      ;(log/debug (count open) open)
      (if-let [x (first open)]
        (recur (conj closed x)
               {:x (min (:x NW-corner) (:x x))
                :y (min (:y NW-corner) (:y x))}
               {:x (max (:x SE-corner) (:x x))
                :y (max (:y SE-corner) (:y x))}
               (if (or (door? x) (wall? x) (missing-wall? level x))
                 (disj open x)
                 (into (disj open x)
                       ; walking triggers more refloods to mark unexplored tiles
                       (remove (some-fn blank? :dug corridor? closed)
                               (neighbors level x)))))
        (room-rectangle game NW-corner SE-corner kind)))))

(defn reflood-room [game pos]
  (let [tile (at-curlvl game pos)]
    (if (and (:room tile) (not (:walked tile)))
      (do (log/debug "room reflood from:" pos "type:" (:room tile))
          (floodfill-room game pos (:room tile)))
      game)))

(defn- closest-roomkeeper
  "Presumes having just entered a room"
  [game]
  (min-by #(distance (:player game) %)
          (filter (partial shopkeeper-look? game)
                  (curlvl-monsters game))))

(def ^:private room-re #"Welcome(?: again)? to(?: (?:[A-Z]\S+|a))+ ([a-z -]+)!")

(defn room-type [msg]
  ; TODO temples, maybe treasure zoos etc.
  (or (if (.endsWith msg ", welcome to Delphi!\"") :oracle)
      (if (re-seq #"Invisible customers are not welcome" msg) :shop)
      (shop-types (re-first-group room-re msg))))

(defn mark-room [game kind]
  (log/debug "marking room as" kind)
  (as-> game res
      (add-curlvl-tag res kind)
      (if-let [roomkeeper (and (shops kind) (closest-roomkeeper res))]
        (floodfill-room res roomkeeper kind)
        res)
      (if (adjacent? (:last-position game) (:player game))
        (update-at res (:last-position game) assoc :room nil)
        res)))

(defn- match-level
  "Returns true if the level matches the blueprint's :dlvl, :branch and :tag
  (if present)"
  [game level blueprint]
  (and (or (not (:role blueprint))
           (= (:role (:player game)) (:role blueprint)))
       (or (not (:branch blueprint))
           (= (:branch blueprint) (branch-key game level)))
       (or (not (:dlvl blueprint))
           (= (:dlvl blueprint) (:dlvl level)))
       (or (not (:tag blueprint))
           ((:tags level) (:tag blueprint)))))

(defn- apply-blueprint [level blueprint]
  (log/debug "applying blueprint" (select-keys blueprint [:branch :tag :dlvl]))
  (as-> level res
    (reduce #(update-at %1 %2 assoc :undiggable true)
            res
            (:undiggable-tiles blueprint))
    (reduce #(update-at %1 %2
                        assoc :feature :rock :undiggable true :seen true)
            res
            (for [x (:cutoff-cols blueprint)
                  y (range 1 22)]
              (position x y)))
    (reduce #(update-at %1 %2
                        assoc :feature :rock :undiggable true :seen true)
            res
            (for [x (range 0 80)
                  y (:cutoff-rows blueprint)]
              (position x y)))
    (reduce (fn mark-feature [level [pos feature]]
              (update-at level pos assoc :seen true :feature
                         (cond (= :door-secret feature)
                               (if ((some-fn unknown? wall?) (at level pos))
                                 :door-secret
                                 (:feature (at level pos)))
                               (= :cloud feature) feature
                               :else (or (:feature (at level pos)) feature))))
            res
            (:features blueprint))
    (reduce (fn add-monster [level [pos monster]]
              (reset-monster level (known-monster (:x pos) (:y pos) monster)))
            res
            (:monsters blueprint))))

(defn- match-blueprint
  "Find and apply a matching blueprint to the level or return nil"
  [game level]
  (when-let [blueprint (find-first (partial match-level game level) blueprints)]
    (log/debug "matched blueprint, level:" (:dlvl level)
               "; branch:" (branch-key game level) "; tags:" (:tags level))
    (-> level
        (assoc :blueprint blueprint)
        (apply-blueprint blueprint))))

(defn level-blueprint
  "If the current level doesn't have a blueprint, check for a match and apply it"
  [game]
  (let [level (curlvl game)]
    (if-let [new-level (and (not (:blueprint level))
                            (match-blueprint game level))]
      (assoc-in game [:dungeon :levels (branch-key game new-level)
                      (:dlvl new-level)] new-level)
      game)))

(defn diggable-walls?
  "Are the walls diggable on this level?"
  [game level]
  (and ;(not= "Home 1" (:dlvl game))
       (not-any? #{:rogue :sanctum :medusa :bigroom} (:tags level))
       (or (:orcus (:tags level)) (not (:undiggable (:blueprint level))))
       (not (#{:vlad :astral :sokoban :quest} (branch-key game level)))))

(defn below-castle?
  "Being on the right side of the castle is also considered below"
  [{:keys [player] :as game}]
  (if-let [castle (get-dlvl game :main :castle)]
    (or (pos? (dlvl-compare (:dlvl game) castle))
        (and (= (:dlvl game) castle)
             (or (< 69 (:x (:player game)))
                 (and (< 64 (:x player)) (< 8 (:y player) 16))
                 (and (<= 60 (:x player)) (= 12 (:y player))))))))

(defn at-planes? [game]
  (planes (branch-key game)))

(defn in-gehennom?
  "Your god won't help you here (includes VoTD)"
  [game]
  (and (#{:wiztower :main} (branch-key game))
       (if-let [castle (get-dlvl game :main :castle)]
         (pos? (dlvl-compare (:dlvl game) castle)))))

(defn below-medusa?
  "Being on the right side of medusa's island is also considered below"
  [game]
  (if-let [medusa (get-dlvl game :main :medusa)]
    (or (pos? (dlvl-compare (:dlvl game) medusa))
        (and (= (:dlvl game) medusa)
             (< 22 (:x (:player game)))))))

(defn apply-default-blueprint [game]
  (if (and (in-gehennom? game) (not (:votd (curlvl-tags game)))
           (some wall? (neighbors (curlvl game) (:player game))))
    (update-curlvl game apply-blueprint geh-maze)
    game))

(defn narrow?
  ([game from to] (narrow? game (curlvl game) from to))
  ([game level from to]
   (if (and (adjacent? from to) (diagonal? from to))
     (every? #(or (rock? %) (wall? %)
                  (and (= :sokoban (branch-key game)) (boulder? %)))
             (intersection (set (straight-neighbors level from))
                           (set (straight-neighbors level to)))))))

(defn edge-passable-walking? [game level from-tile to-tile]
  (or (straight (towards from-tile to-tile))
      (and (diagonal-walkable? game from-tile)
           (diagonal-walkable? game to-tile)
           (or (and (not= :sokoban (branch-key game level))
                    (not (:thick (:player game))))
               (not (narrow? game level from-tile to-tile))))))

(defn passable-walking?
  "Only needs Move action, no door opening etc., will path through monsters and
  unexplored tiles"
  [game level from-tile to-tile]
  (and (walkable? to-tile)
       (edge-passable-walking? game level from-tile to-tile)))

; branch ID is either a branch keyword from branches or random keyword that will map (via id->branch) to a standard branch keyword when the level branch is recognized.
; a Level should be permanently uniquely identified by its branch-id + dlvl.
(defrecord Dungeon
  [levels ; {:branch-id => sorted{"dlvl" => Level}}, recognized branches merged
   id->branch] ; {:branch-id => :branch}, only ids of recognized levels included
  bothack.bot.dungeon.IDungeon
  (^bothack.bot.dungeon.ILevel
    getLevel [dungeon ^bothack.bot.dungeon.Branch branch-id ^bothack.bot.dungeon.LevelTag tag]
    (get-level {:dungeon dungeon} (.getKeyword branch-id) (.getKeyword tag)))
  (^bothack.bot.dungeon.ILevel
    getLevel [dungeon ^bothack.bot.dungeon.Branch branch-id ^String dlvl]
    (get-level {:dungeon dungeon} (.getKeyword branch-id) dlvl))
  (getBranch [dungeon branch-id]
    (get-branch {:dungeon dungeon} (.getKeyword branch-id))))

(defn new-dungeon []
  (Dungeon. {} (reduce #(assoc %1 %2 %2) {} branches)))
