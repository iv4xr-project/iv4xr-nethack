(ns bothack.bots.wizbot
  "a bot that can ascend in wizmode (when properly pre-equipped)"
  (:require [clojure.tools.logging :as log]
            [flatland.ordered.set :refer [ordered-set]]
            [bothack.bothack :refer :all]
            [bothack.item :refer :all]
            [bothack.itemid :refer :all]
            [bothack.handlers :refer :all]
            [bothack.player :refer :all]
            [bothack.pathing :refer :all]
            [bothack.monster :refer :all]
            [bothack.position :refer :all]
            [bothack.game :refer :all]
            [bothack.dungeon :refer :all]
            [bothack.level :refer :all]
            [bothack.tile :refer :all]
            [bothack.delegator :refer :all]
            [bothack.util :refer :all]
            [bothack.behaviors :refer :all]
            [bothack.tracker :refer :all]
            [bothack.actions :refer :all]))

(def hostile-dist-thresh 4)

(defn- hostile-threats [{:keys [player] :as game}]
  (->> (curlvl-monsters game)
       (filter #(and (hostile? %)
                     (not (and (blind? player) (:remembered %)))
                     (or (adjacent? player %)
                         (and (> 10 (- (:turn game) (:known %)))
                              (> hostile-dist-thresh (distance player %))
                              (not (blind? player))
                              (not (hallu? player))
                              (not (:fleeing %))
                              (not (digit? %))))))
       set))

(defn- handle-starvation [{:keys [player] :as game}]
  (or (if (weak? player)
        (if-let [[slot food] (have game (every-pred (partial edible? player)
                                                    (complement tin?))
                                   #{:bagged})]
          (with-reason "weak or worse, eating" food
            (or (unbag game slot food)
                (->Eat slot)))))
      (if (and (fainting? (:player game))
               (can-pray? game))
        (with-reason "praying for food" ->Pray))))

(defn- handle-illness [{:keys [player] :as game}]
  (or (if-let [[slot _] (and (unihorn-recoverable? game)
                             ; rest can wait
                             (some (:state player) #{:conf :stun :ill :blind})
                             (have-unihorn game))]
        (with-reason "applying unihorn to recover" (->Apply slot)))
      (if (:ill (:state player))
        (with-reason "fixing illness"
          (or (if-let [[slot _] (have game "eucalyptus leaf" #{:noncursed})]
                (->Eat slot))
              (if-let [[slot _] (or (have game "potion of healing"
                                          {:buc :blessed})
                                    (have game #{"potion of extra healing"
                                                 "potion of full healing"}
                                          #{:noncursed}))]
                (->Quaff slot)))))))

(defn- handle-impairment [{:keys [player] :as game}]
  (or (if (:lycantrophy player)
        (if-not (in-gehennom? game)
          (with-reason "curing lycantrophy" ->Pray)))
      (if-let [[slot _] (and (unihorn-recoverable? game)
                             (have-unihorn game))]
        (with-reason "applying unihorn to recover" (->Apply slot)))
      (if (impaired? player)
        (with-reason "waiting out impairment" ->Wait))))

(defn name-first-amulet [bh]
  (reify ActionHandler
    (choose-action [this game]
      (when-let [[slot _] (have game "Amulet of Yendor")]
        (deregister-handler bh this)
        (with-reason "naming the real amulet"
          (->Name slot "REAL"))))))

(defn real-amulet? [item]
  (and (= "Amulet of Yendor" (:name item))
       (= "REAL" (:specific item))))

(defn get-amulet [game]
  (if-not (have game real-amulet?)
    (with-reason "searching for the amulet"
      (or (explore game)
          (search-level game 1) ; if Rodney leaves the level with it we're screwed
          (seek game stairs-up?)))))

(defn full-explore [game]
  (if-not (get-level game :main :sanctum)
    (or (explore game :mines)
        ;(explore game :sokoban)
        (explore game :quest)
        (explore game :vlad)
        (explore game :main)
        (explore game :wiztower)
        (invocation game))))

(defn endgame? [game]
  (get-level game :main :sanctum))

(defn progress [game]
  (if-not (endgame? game)
    (or #_(full-explore game)
        (explore-level game :mines :minetown)
        (visit game :mines :end)
        (visit game :main :medusa)
        ;(explore-level game :sokoban :end)
        (explore-level game :quest :end)
        (explore-level game :main :castle)
        (explore-level game :vlad :end)
        (explore-level game :main :end)
        (explore-level game :wiztower :end)
        (invocation game))
    (or (get-amulet game)
        (visit game :astral)
        (seek-high-altar game))))

(defn- pause-condition?
  "For debugging - pause the game when something occurs"
  [game]
  #_(= :astral (branch-key game))
  #_(= "Dlvl:46" (:dlvl game))
  #_(explored? game :main :end)
  #_(and (= :wiztower (branch-key game))
       (:end (curlvl-tags game))
  #_(and (= :vlad (branch-key game))
       (:end (curlvl-tags game)))
  #_(have game candelabrum)
  #_(have game "Orb of Fate")
  ))

(def desired-weapons
  (ordered-set "Grayswandir" "Excalibur" "Mjollnir" "Stormbringer"
               "katana" "long sword"))

(def desired-suit
  (ordered-set "gray dragon scale mail" "silver dragon scale mail" "dwarwish mithril-coat" "elven mithril-coat" "scale mail"))

(def desired-boots
  (ordered-set "speed boots" "iron shoes"))

(def desired-shield
  (ordered-set "shield of reflection" "small shield"))

(def desired-cloak
  #{"oilskin cloak"})

(def desired-items
  [(ordered-set "pick-axe" #_"dwarvish mattock") ; currenty-desired presumes this is the first category
   (ordered-set "skeleton key" "lock pick" "credit card")
   (ordered-set "ring of levitation" "boots of levitation")
   #{"ring of slow digestion"}
   #{"Orb of Fate"}
   (ordered-set "blindfold" "towel")
   #{"unicorn horn"}
   #{"Candelabrum of Invocation"}
   #{"Bell of Opening"}
   #{"Book of the Dead"}
   #{"lizard corpse"}
   desired-cloak
   desired-suit
   desired-shield
   desired-boots
   #{"amulet of reflection"}
   #{"amulet of unchanging"}
   desired-weapons])

(defn currently-desired
  "Returns the set of item names that the bot currently wants.
  Assumes the bot has at most 1 item of each category."
  [game]
  (loop [cs (if (or (entering-shop? game) (shop? (at-player game)))
              (rest desired-items) ; don't pick that pickaxe back up
              desired-items)
         res #{}]
    (if-let [c (first cs)]
      (if-let [[slot i] (have game c)]
        (recur (rest cs)
               (into res (take-while (partial not= (item-name game i)) c)))
        (recur (rest cs) (into res c)))
      (or (if-let [sanctum (get-level game :main :sanctum)]
            (if (and (not (have game real-amulet?))
                     (:seen (at sanctum 20 11)))
              (conj res "Amulet of Yendor")))
          res))))

(defn consider-items [game]
  (let [desired (currently-desired game)
        to-take? #(or (real-amulet? %)
                      (and (desired (item-name game %)) (can-take? %)))]
    (or (if-let [to-get (seq (for [item (lootable-items (at-player game))
                                   :when (to-take? item)]
                               (:label item)))]
          (with-reason "looting desirable items"
            (without-levitation game
              (take-out \. (->> to-get set vec))))
          (log/debug "no desired lootable items"))
        (if-let [to-get (seq (for [item (:items (at-player game))
                                   :when (to-take? item)]
                               (:label item)))]
          (with-reason "getting desirable items"
            (without-levitation game
              (->PickUp (->> to-get set vec))))
          (log/debug "no desired items here"))
        (when-let [{:keys [step target]}
                   (navigate game #(some to-take? (concat (:items %)
                                                          (lootable-items %))))]
          (with-reason "want item at" target step))
        (log/debug "no desirable items anywhere"))))

(defn uncurse-weapon [game]
  (if-let [[_ weapon] (wielding game)]
    (if-let [[slot scroll] (and (cursed? weapon)
                                (have game "scroll of remove curse"
                                      #{:noncursed :bagged}))]
      (with-reason "uncursing weapon" (:label weapon)
        (or (unbag game slot scroll)
            (->Read slot))))))

(defn- wield-weapon [{:keys [player] :as game}]
  (if-let [[slot weapon] (some (partial have-usable game) desired-weapons)]
    (if-not (:wielded weapon)
      (or (uncurse-weapon game)
          (with-reason "wielding better weapon -" (:label weapon)
            (->Wield slot))))))

(defn- wear-armor [{:keys [player] :as game}]
  (first (for [category [desired-shield desired-boots
                         desired-suit desired-cloak]
               :let [[slot armor] (some (partial have-usable game) category)]
               :when (and armor (not (:worn armor)))]
           (with-reason "wearing better armor"
             (make-use game slot)))))

(defn light? [game item]
  (let [id (item-id game item)]
    (and (not= "empty" (:specific item))
         (= :light (:subtype id))
         (= :copper (:material id)))))

(defn bless-gear [game]
  (or (if-let [[slot item] (have game #{"Orb of Fate" "unicorn horn"
                                        "luckstone" "bag of holding"}
                                 #{:nonblessed :know-buc})]
        (if-let [[water-slot water] (have game holy-water? #{:bagged})]
          (or (unbag game water-slot water)
              (with-reason "blessing" item
                (->Dip slot water-slot)))))
      (if-let [[_ item] (have game (every-pred cursed? :in-use))]
        (if-let [[slot scroll] (have game "scroll of remove curse"
                                     #{:noncursed :bagged})]
          (with-reason "uncursing" (:label item)
            (or (unbag game slot scroll)
                (->Read slot)))))))

(defn lit-mines? [game level]
  (and (= :mines (branch-key game))
       (if-let [floors (seq (filter #(and (floor? %)
                                          (< 5 (distance % (:player game))))
                                    (tile-seq level)))]
         (not-any? blank? floors))))

(defn- want-light? [game level]
  (not (or (explored? game)
           (:minetown (:tags level))
           (#{:air :water} (branch-key game))
           (lit-mines? game level))))

(defn use-light [game level]
  (if-let [[slot item] (have game (every-pred :lit (partial light? game)))]
    (if (and (not= "magic lamp" (item-name game item))
             (not (want-light? game level)))
      (with-reason "saving energy" (->Apply slot)))
    (if (want-light? game level)
      (or (if-let [[slot lamp] (have game "magic lamp")]
            (with-reason "using magic lamp" (->Apply slot)))
          (if-let [[slot lamp] (have game (partial light? game))]
            (with-reason "using any light source" (->Apply slot)))))))

(defn reequip [game]
  (let [level (curlvl game)
        tile-path (mapv (partial at level) (:last-path game))
        step (first tile-path)
        branch (branch-key game)]
    (or (bless-gear game)
        (wear-armor game)
        (if (and (not= :wield (some-> game :last-action typekw))
                 step (not (:dug step))
                 (every? walkable? tile-path))
          (if-let [[slot item] (and (#{:air :fire :earth} branch)
                                    (not-any? portal? (tile-seq level))
                                    (have game real-amulet?))]
            (if-not (:in-use item)
              (with-reason "using amulet to search for portal"
                (->Wield slot)))
            (with-reason "reequip - weapon"
              (wield-weapon game))))
        ; TODO multidrop
        (if-let [[slot _] (have game #(= "empty" (:specific %)))]
          (with-reason "dropping junk" (->Drop slot)))
        (use-light game level)
        (if-let [[slot _] (and (not (needs-levi? (at-player game)))
                               (not (#{:water :air} branch))
                               (not-any? needs-levi? tile-path)
                               (have-levi-on game))]
          (with-reason "reequip - don't need levi"
            (remove-use game slot))))))

(defn- bait-wizard [game level monster]
  (if (and (= :magenta (:color monster)) (= \@ (:glyph monster))
           (not= :water (branch-key game))
           ((some-fn pool? lava?) (at level monster)))
    (with-reason "baiting possible wizard away from water/lava"
      ; don't let the book fall into water/lava
      (or (:step (navigate game #(every? (not-any-fn? lava? pool?)
                                         (neighbors level %))))
          (->Wait)))))

(defn- bait-giant [game level monster]
  (if (and (= \H (:glyph monster)) (= "Home 3" (:dlvl level))
           (not (have-pick game)) (= 12 (:y monster))
           (< 18 (:x monster) 25))
    ; only needed until the bot can use wand of striking to break blocking boulders
    (with-reason "baiting giant away from corridor"
      (or (:step (navigate game #{(position 26 12)
                                  (position 16 12)}))
          (->Wait)))))

(defn- hit [game level player monster]
  (with-reason "hitting" monster
    (or (bait-wizard game level monster)
        (bait-giant game level monster)
        (wield-weapon game)
        (if-let [[slot _] (and (= :air (branch-key game))
                               (not (have-levi-on game))
                               (have-levi game))]
          (with-reason "levitation for :air"
            (make-use game slot)))
        (if (adjacent? player monster)
          (if (or (not (monster? (at level monster)))
                  (#{\I \1 \2 \3 \4 \5} (:glyph monster)))
            (->Attack (towards player monster))
            (->Move (towards player monster)))))))

(defn fight [{:keys [player] :as game}]
  (or (if (:engulfed player)
        (with-reason "killing engulfer" (or (wield-weapon game)
                                            (->Move :E))))
      (let [level (curlvl game)
            adjacent (->> (neighbors player)
                          (keep (partial monster-at level))
                          (filter hostile?))]
        (if-let [monster (or (if (some pool? (neighbors level player))
                               (find-first drowner? adjacent))
                             (find-first rider? adjacent)
                             (find-first unique? adjacent)
                             (find-first priest? adjacent)
                             (find-first nasty? adjacent))]
          (hit game level player monster)))
      (when-let [{:keys [step target]} (navigate game (hostile-threats game)
                                                 {:adjacent true :no-traps true
                                                  :walking true :no-autonav true
                                                  :max-steps
                                                  (if (at-planes? game)
                                                    1
                                                    hostile-dist-thresh)})]
        (let [level (curlvl game)
              monster (monster-at level target)]
          (with-reason "targetting enemy" monster
            (or (hit game level player monster)
                step))))))

(defn- bribe-demon [prompt]
  (->> prompt
       (re-first-group #"demands ([0-9][0-9]*) zorkmids for safe passage")
       parse-int))

(defn- pause-handler [bh]
  (reify FullFrameHandler
    (full-frame [_ _]
      (when (pause-condition? @(:game bh))
        (log/debug "pause condition met")
        (pause bh)))))

(defn- feed [{:keys [player] :as game}]
  (if-not (satiated? player)
    (let [beneficial? #(every-pred
                         (partial fresh-corpse? game %)
                         (partial want-to-eat? player))
          edible? #(every-pred
                     (partial fresh-corpse? game %)
                     (partial edible? player))]
      (or (if-let [p (navigate game #(some (beneficial? %) (:items %)))]
            (with-reason "want to eat corpse at" (:target p)
              (or (:step p)
                  (->> (at-player game) :items
                       (find-first (beneficial? player)) :label
                       ->Eat
                       (without-levitation game)))))
          (if true #_(hungry? player) ; TODO eat tins
            (if-let [p (navigate game #(some (edible? %) (:items %)))]
              (with-reason "going to eat corpse at" (:target p)
                (or (:step p)
                    (->> (at-player game) :items
                         (find-first (edible? player)) :label
                         ->Eat
                         (without-levitation game))))))))))

(defn offer-amulet [game]
  (let [tile (and (= :astral (:branch-id game))
                  (at-player game))]
    (if (and (altar? tile) (= (:alignment (:player game)) (:alignment tile)))
      (some-> (have game real-amulet?) key ->Offer))))

(defn detect-portal [bh]
  (reify ActionHandler
    (choose-action [this {:keys [player] :as game}]
      (if-let [[scroll s] (and (= :water (branch-key game))
                               (have game "scroll of gold detection"
                                     #{:safe-buc :bagged}))]
        (with-reason "detecting portal"
          (or (unbag game scroll s)
              (when (confused? player)
                (deregister-handler bh this)
                (->Read scroll))
              (if-let [[potion p] (and (not-any? #(and (> 4 (distance player %))
                                                       (hostile? %))
                                                 (curlvl-monsters game))
                                       (have game #{"potion of confusion"
                                                    "potion of booze"}
                                             #{:nonblessed :bagged}))]
                (with-reason "confusing self"
                  (or (unbag game potion p)
                      (->Quaff potion))))))))))

(defn init [bh]
  (-> bh
      (register-handler priority-bottom (pause-handler bh))
      (register-handler (reify
                          OfferHandler
                          (offer-how-much [_ _]
                            (bribe-demon (:last-topline @(:game bh))))
                          ReallyAttackHandler
                          (really-attack [_ _] false)))
      ; expensive action-decision handlers could easily be aggregated and made to run in parallel as thread-pooled futures, dereferenced in order of their priority and cancelled when a decision is made
      (register-handler -99 (reify ActionHandler
                              (choose-action [_ game]
                                (offer-amulet game))))
      (register-handler -15 (reify ActionHandler
                              (choose-action [_ game]
                                (enhance game))))
      (register-handler -14 (name-first-amulet bh))
      (register-handler -10 (reify ActionHandler
                              (choose-action [_ game]
                                (handle-starvation game))))
      (register-handler -8 (detect-portal bh))
      (register-handler -7 (reify ActionHandler
                             (choose-action [_ game]
                               (handle-illness game))))
      (register-handler -5 (reify ActionHandler
                             (choose-action [_ game]
                               (fight game))))
      (register-handler -3 (reify ActionHandler
                             (choose-action [_ game]
                               (handle-impairment game))))
      (register-handler 0 (reify ActionHandler
                            (choose-action [_ game]
                              (reequip game))))
      (register-handler 1 (reify ActionHandler
                            (choose-action [_ game]
                              (feed game))))
      (register-handler 2 (reify ActionHandler
                            (choose-action [_ game]
                              (consider-items game))))
      (register-handler 5 (reify ActionHandler
                            (choose-action [_ game]
                              (progress game))))))
