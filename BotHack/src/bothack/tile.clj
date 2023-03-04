(ns bothack.tile
  (:require [clojure.tools.logging :as log]
            [clojure.pprint :as pprint]
            [bothack.position :refer [neighbors]]
            [bothack.item :refer :all]
            [bothack.util :refer :all]))

(defn digit? [tile]
  (Character/isDigit ^Character (:glyph tile)))

(defn monster-glyph? [glyph]
  (or (and (Character/isLetterOrDigit ^Character glyph)
           (not= \8 glyph) (not= \0 glyph))
      (#{\& \@ \' \; \: \~} glyph)))

(defn monster?
  ([tile]
   (monster? (:glyph tile) (:color tile)))
  ([glyph color] ; works better on rogue level and for worm tails
   (or (and (not= \~ glyph) (monster-glyph? glyph) (or color (not= \: glyph)))
       (and (= \~ glyph) (= :brown color)))))

; bot should never get to see :trap - auto-examine
(def traps #{:trap :antimagic :arrowtrap :beartrap :darttrap :firetrap :hole :magictrap :rocktrap :mine :levelport :pit :polytrap :portal :bouldertrap :rusttrap :sleeptrap :spikepit :squeaky :teletrap :trapdoor :web :statuetrap})

(def trap-names
  {"magic portal" :portal
   "level teleporter" :levelport
   "teleportation trap" :teletrap
   "bear trap" :beartrap
   "falling rock trap" :rocktrap
   "rolling boulder trap" :bouldertrap
   "rust trap" :rusttrap
   "magic trap" :magictrap
   "anti-magic field" :antimagic
   "polymorph trap" :polytrap
   "fire trap" :firetrap
   "arrow trap" :arrowtrap
   "statue trap" :statuetrap
   "land mine" :mine
   "dart trap" :darttrap
   "sleeping gas trap" :sleeptrap
   "spider web" :web
   "web" :web
   "squeaky board" :squeaky
   "hole" :hole
   "trap door" :trapdoor
   "pit" :pit
   "spiked pit" :spikepit})

(defn item?
  ([tile] (item? (:glyph tile) (:color tile)))
  ([glyph color]
   (or (#{\" \) \[ \! \? \/ \= \+ \* \( \` \8 \0 \$ \% \,} glyph)
       (and (some? color) (= \_ glyph))
       (and (nil? color) (or (= \: glyph) (= \] glyph))))))

(defn dug? [tile]
  (:dug tile))

(defn unknown? [tile]
  (nil? (:feature tile)))

(defn boulder? [tile]
  (and (= (:glyph tile) \8) (nil? (:color tile))))

(defn door? [tile]
  (#{:door-open :door-closed :door-locked :door-secret} (:feature tile)))

(defn drawbridge? [tile]
  (#{:drawbridge-lowered :drawbridge-raised} (:feature tile)))

(defn stairs? [tile]
  (#{:stairs-up :stairs-down} (:feature tile)))

(defn opposite-stairs [feature]
  {:pre [(#{:stairs-up :stairs-down} feature)]}
  (if (= :stairs-up feature)
    :stairs-down
    :stairs-up))

(defn has-feature? [tile feature]
  (= feature (:feature tile)))

(defmacro ^:private def-feature-pred [feature]
  `(defn ~(symbol (str (.getName feature) \?)) [~'tile]
     (has-feature? ~'tile ~feature)))

#_(pprint (macroexpand-1 '(def-feature-pred :wall)))

(defmacro ^:private def-feature-preds []
  `(do ~@(for [feature (concat traps
                               [:rock :floor :wall :stairs-up :stairs-down
                                :corridor :altar :pool :door-open :door-closed
                                :door-locked :door-secret :sink :grave :throne
                                :bars :drawbridge-raised :drawbridge-lowered
                                :lava :ice :portal :tree :trapdoor :hole
                                :firetrap :cloud :polytrap])]
           `(def-feature-pred ~feature))))

(def-feature-preds)

(defn fountain?
  "Dangerously overused minetown fountains not considered fountains"
  [tile]
  (and (has-feature? tile :fountain)
       (not (:trickle (:tags tile)))))

(defn trap?
  "Clouds on the plane of air are considered a trap (unavoidable lightning)"
  [tile]
  (or (traps (:feature tile)) (cloud? tile)))

(defn unknown-trap? [tile]
  (= :trap (:feature tile)))

(defn blank? [tile]
  (= \space (:glyph tile)))

(defn walkable?
  "Considers unexplored tiles, traps and ice walkable"
  [tile]
  (and (not (boulder? tile))
       (or (unknown? tile)
           (trap? tile)
           (#{:ice :floor :altar :door-open :sink :fountain :corridor :throne
              :grave :stairs-up :stairs-down :drawbridge-lowered :cloud}
                   (:feature tile)))))

(defn diagonal-walkable? [game tile]
  (not (door-open? tile)))

(defn transparent?
  "For unexplored tiles just a guess"
  [{:keys [feature monster items] :as tile}]
  (and (not (boulder? tile))
       (not (#{:rock :wall :tree :door-closed :cloud} feature))
       (or feature monster (seq items))))

(defn diggable? [tile]
  (or (boulder? tile)
      (and (#{:rock :wall :door-closed :door-locked :door-secret}
                    (:feature tile))
           (< 0 (:x tile) 79)
           (< 0 (:y tile) 21)
           (not (:undiggable tile)))))

(defn searched
  "How many times the tile has been searched directly (not by searching a neighbor)"
  [level tile]
  (apply min (map :searched (neighbors level tile))))

(defn walkable-by [{:keys [feature] :as tile} glyph]
  (assoc tile
         :feature (cond
                    (and (not (#{\I \1 \2 \3 \4 \5 \E \X \P} glyph))
                         (door? tile)) :door-open
                    (and (not (#{\I \1 \2 \3 \4 \5 \E \X} glyph))
                         (#{:rock :wall :tree :drawbridge-raised}
                          feature)) nil ; could be just-found door or corridor
                    :else feature)
         :dug (or (:dug tile)
                  (and (diggable? tile) (#{\U \p \h \r} glyph)))))

(defn- door-or-wall [current new-color]
  (cond
    (= new-color :brown) :door-open
    (= :door-secret current) :door-secret
    :else :wall))

(defn- infer-feature [current new-glyph new-color]
  (case new-glyph
    \space current
    \. (if (traps current)
         current
         (case new-color
           :cyan :ice
           :brown :drawbridge-lowered
           :floor))
    \< :stairs-up
    \> :stairs-down
    \\ (if (= new-color :yellow) :throne :grave)
    \{ (if (nil? new-color) :sink :fountain)
    \} (case new-color
         :green :tree
         :red :lava
         :cyan :bars
         :blue :pool
         :brown :drawbridge-raised
         (do (log/warn "unknown } feature color:" new-color) current))
    \# (cond (traps current) current
             (= :cloud current) :cloud
             :else :corridor)
    \_ (if (nil? new-color) :altar current)
    \~ :pool
    \^ (if (traps current) current :trap)
    \] :door-closed
    \| (door-or-wall current new-color)
    \- (door-or-wall current new-color)))

; they might not have actually been seen but there's usually not much to see in walls/water
(defn- mark-seen-features [tile]
  (if (#{:wall :door-closed :pool :lava} (:feature tile))
    (assoc tile :seen true)
    tile))

(defn- update-feature-with-item [tile]
  ; if items appeared in rock/wall/doors we should check it out
  (if (and (empty? (:items tile)) (#{:rock :wall :door-closed :door-locked
                                     :drawbridge-raised :door-secret :pool}
                                           (:feature tile)))
    (assoc tile :feature nil)
    tile))

(defn reset-item [tile]
  ;(log/debug "reset item" tile)
  (assoc tile
         :item-color nil
         :item-glyph nil
         :items []
         :new-items false))

(defn- mark-item [tile new-glyph new-color]
  (if (= \8 new-glyph)
    (if (monster? tile)
      (assoc tile :new-items true)
      tile)
    (if (and (= new-glyph (:item-glyph tile))
             (or (not (:item-color tile)) ; don't have data to infer color from item appearance
                 (= new-color (:item-color tile))))
      (assoc tile :item-color new-color) ; unchanged
      (assoc tile
             :new-items true
             :item-glyph new-glyph
             :item-color new-color))))

(defn- update-items [tile new-glyph new-color]
  (cond (item? new-glyph new-color) (-> tile
                                        update-feature-with-item
                                        (mark-item new-glyph new-color))
        (or (monster? new-glyph new-color) (= \space new-glyph)) tile
        :else (reset-item tile)))

(defn- update-feature [tile new-glyph new-color]
  (cond (monster? new-glyph new-color) (walkable-by tile new-glyph)
        (item? new-glyph new-color) tile
        :else (update tile :feature infer-feature new-glyph new-color)))

(defn- mark-dug-tile [new-tile old-tile]
  (if (and (zero? (:searched new-tile))
           (#{:wall :rock} (:feature old-tile))
           (#{:corridor :floor} (:feature new-tile)))
    (assoc new-tile :dug true)
    new-tile))

(defn parse-tile [tile new-glyph new-color]
  (if (or (not= new-color (:color tile)) (not= new-glyph (:glyph tile)))
    (-> tile
        (update-items new-glyph new-color)
        (update-feature new-glyph new-color)
        (mark-dug-tile tile)
        (assoc :glyph new-glyph :color new-color)
        (assoc :thump nil)
        mark-seen-features)
    tile))

(def shop-types
  {"general store" :general
   "used armor dealership" :armor
   "second-hand bookstore" :book
   "liquor emporium" :potion
   "antique weapons outlet" :weapon
   "delicatessen" :food
   "jewelers" :gem
   "quality apparel and accessories" :wand
   "hardware store" :tool
   "rare books" :book
   "lighting store" :light})

(def shops (apply hash-set :shop (vals shop-types)))

(defn shop? [tile]
  (shops (:room tile)))

(defn temple?
  "Only true near the altar"
  [tile]
  (= :temple (:room tile)))

(defn mark-death [tile monster turn]
  (-> tile
      (update :deaths conj [turn monster])
      (assoc :new-items true)))

(defn lootable-items [tile]
  (for [container (:items tile)
        :when (not (:cost container))
        item (:items container)]
    item))

(defn e?
  "Is Elbereth inscribed on the current tile?"
  [tile]
  (and (:engraving tile) (.contains (:engraving tile) "Elbereth")))

(defn perma-e?
  "Is Elbereth engraved on the current tile with a permanent method?"
  [tile]
  (and (e? tile) (= :permanent (:engraving-type tile))))

(defn engravable?
  "Is the tile safely dust-engravable?  Considers water/air plane tiles
  engravable (can-engrave? catches this case)"
  [tile]
  (and (walkable? tile)
       ((not-any-fn? pool? lava? fountain? altar? grave?) tile)
       (or (not (:engraving-type tile))
           (= :dust (:engraving-type tile)))))

(defn temple? [tile]
  (= :temple (:room tile)))

(defn visited-stairs? [tile]
  (and (stairs? tile) (:branch-id tile)))

(defn unexplored? [tile]
  (and (not (boulder? tile)) (unknown? tile)))

(defn blocked?
  "Stubborn peacefuls"
  [tile]
  (> (or (:blocked tile) 0) 12))

(defrecord Tile
  [x y
   glyph
   color
   item-glyph
   item-color
   feature ; :rock :floor :wall :stairs-up :stairs-down :corridor :altar :pool :door-open :door-closed :door-locked :door-secret :sink :fountain :grave :throne :bars :tree :drawbridge-raised :drawbridge-lowered :lava :ice + traps
   seen
   first-walked ; turn no.
   walked ; turn no.
   dug
   searched ; no. of times searched
   items ; [Item]
   new-items ; flag if some items changed
   engraving
   engraving-type ; :dust :semi :permanent
   deaths ; [ turn Monster ], deaths that left no corpse are ignored
   tags
   room]
  bothack.bot.IPosition
  (x [pos] (:x pos))
  (y [pos] (:y pos))
  bothack.bot.IAppearance
  (glyph [tile] (:glyph tile))
  (color [tile] (kw->enum bothack.bot.Color (:color tile)))
  bothack.bot.dungeon.ITile
  (hasElbereth [tile] (boolean (e? tile)))
  (hasBoulder [tile] (boolean (boulder? tile)))
  (isEngravable [tile] (boolean (engravable? tile)))
  (isTrap [tile] (boolean (trap? tile)))
  (isVibrating [tile] (boolean (:vibrating tile)))
  (feature [tile] (kw->enum bothack.bot.dungeon.Feature (:feature tile)))
  (wasSeen [tile] (boolean (:seen tile)))
  (firstWalkedTurn [tile] (:first-walked tile))
  (lastWalkedTurn [tile] (:walked tile))
  (dug [tile] (boolean (:dug tile)))
  (searched [tile] (:searched tile))
  (items [tile] (:items tile))
  (hasNewItems [tile] (boolean (:new-items tile)))
  (engraving [tile] (:engraving tile))
  (engravingType [tile] (kw->enum bothack.bot.dungeon.EngravingType
                                  (:engraving-type tile)))
  (room [tile] (kw->enum bothack.bot.dungeon.RoomType (:room tile)))
  (sinkGaveRing [tile] (some? (:ring (:tags tile))))
  (sinkGaveFoocubus [tile] (some? (:foocubus (:tags tile))))
  (sinkGavePudding [tile] (some? (:pudding (:tags tile))))
  (isBlocked [tile] (boolean (blocked? tile)))
  (leadsTo [tile] (kw->enum bothack.bot.dungeon.Branch (:branch-id tile)))
  (altarAlignment [tile] (kw->enum bothack.bot.Alignment (:alignment tile))))

(defn initial-tile [x y]
  (map->Tile {:x x
              :y y
              :glyph \space
              :seen false
              :dug false
              :searched 0
              :items []
              :deaths []
              :new-items false
              :tags #{}}))

(defmethod print-method Tile [tile w]
  (.write w (str "#bothack.game.Tile"
                 (assoc (-> tile (.without :items) (.without :deaths))
                        :deaths-trimmed (take 20 (:deaths tile))
                        :items-trimmed (take 20 (:items tile))))))
