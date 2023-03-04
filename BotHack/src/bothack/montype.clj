(ns bothack.montype
  (:require [clojure.tools.logging :as log]
            [clojure.string :as string]
            [bothack.util :refer :all]))

(defn passive-type? [monster]
  (every? #(= :passive (:type %)) (:attacks monster)))

(defn corrosive-type?
  "Corrodes weapon passively when hit?"
  [montype]
  (some #(and (= :passive (:type %))
              (#{:corrode :acid} (:damage-type %)))
        (:attacks montype)))

(defn has-drowning-attack? [m]
  (some #(= :wrap (:damage-type %)) (:attacks m)))

(defrecord MonsterType
  [name
   glyph
   color
   difficulty
   speed
   ac
   mr
   alignment
   gen-flags
   attacks
   weight
   nutrition
   sounds
   size
   resistances
   resistances-conferred
   tags]
  bothack.bot.monsters.IMonsterType
  (name [m] (:name m))
  (difficulty [m] (:difficulty m))
  (speed [m] (:speed m))
  (AC [m] (:ac m))
  (MR [m] (:mr m))
  (alignment [m] (:alignment m))
  (nutrition [m] (:nutrition m))
  (resistances [m] (:resistances m))
  (conferredResistances [m] (:resistances-conferred m))
  (isPoisonous [m] (boolean (:poisonous (:tags m))))
  (isUnique [m] (boolean (:unique (:gen-flags m))))
  (hasHands [m] (not (or (:nohands (:tags m)) (:nolimbs (:tags m)))))
  (isHostile [m] (boolean (:hostile (:tags m))))
  (isCovetous [m] (boolean (:covetous (:tags m))))
  (respectsElbereth [m] (not (:elbereth (:resistances m))))
  (seesInvisible [m] (boolean (:see-invis (:tags m))))
  (isFollower [m] (boolean (:follows (:tags m))))
  (isWerecreature [m] (boolean (:were (:tags m))))
  (isMimic [m] (.contains (:name m) " mimic"))
  (isPriest [m] (.contains (:name m) "priest"))
  (isShopkeeper [m] (= "shopkeeper" (:name m)))
  (isPassive [m] (boolean (passive-type? m)))
  (isCorrosive [m] (boolean (corrosive-type? m)))
  (isSessile [m] (boolean (:sessile (:tags m))))
  (hasDrowningAttack [m] (boolean (has-drowning-attack? m)))
  (isRider [m] (boolean (:rider (:tags m))))
  (isUndead [m] (boolean (:undead (:tags m))))
  (isStrong [m] (boolean (:strong (:tags m))))
  (isNasty [m] (boolean (:nasty (:tags m))))
  (isHuman [m] (boolean (:human (:tags m))))
  (isGuard [m] (boolean (:guard (:tags m))))
  (isMindless [m] (boolean (:mindless (:tags m))))
  (canBeSeenByInfravision [m] (boolean (:infravisible (:tags m))))
  bothack.bot.IAppearance
  (glyph [tile] (:glyph tile))
  (color [tile] (kw->enum bothack.bot.Color (:color tile))))

(defrecord MonsterAttack
  [type
   damage-type
   dices
   sides])

(def ranged #{:spit :breath :gaze}) ; ranged attack types

(def monster-types [
  (MonsterType. "giant ant", \a, :brown, 2, 18, 3, 0, 0, #{:genocidable :sgroup}, [(MonsterAttack. :bite :physical 1 4)], 10, 10, :silent, :tiny, #{}, #{}, #{:animal :nohands :oviparous :carnivore :hostile})
  (MonsterType. "killer bee", \a, :yellow, 1, 18, -1, 0, 0, #{:genocidable :lgroup}, [(MonsterAttack. :sting :poison 1 3)], 1, 5, :buzz, :tiny, #{:poison}, #{:poison}, #{:animal :fly :nohands :poisonous :hostile :female :oviparous})
  (MonsterType. "soldier ant", \a, :blue, 3, 18, 3, 0, 0, #{:genocidable :sgroup}, [(MonsterAttack. :bite :physical 2 4) (MonsterAttack. :sting :poison 3 4)], 20, 5, :silent, :tiny, #{:poison}, #{:poison}, #{:animal :nohands :oviparous :poisonous :carnivore :hostile})
  (MonsterType. "fire ant", \a, :red, 3, 18, 3, 10, 0, #{:genocidable :sgroup}, [(MonsterAttack. :bite :physical 2 4) (MonsterAttack. :bite :fire 2 4)], 30, 10, :silent, :tiny, #{:fire}, #{:fire}, #{:animal :nohands :oviparous :carnivore :hostile :infravisible})
  (MonsterType. "giant beetle", \a, :blue, 5, 6, 4, 0, 0, #{:genocidable}, [(MonsterAttack. :bite :physical 3 6)], 10, 10, :silent, :large, #{:poison}, #{:poison}, #{:animal :nohands :poisonous :carnivore :hostile})
  (MonsterType. "queen bee", \a, :magenta, 9, 24, -4, 0, 0, #{:genocidable :not-generated}, [(MonsterAttack. :sting :poison 1 8)], 1, 5, :buzz, :tiny, #{:poison}, #{:poison}, #{:animal :fly :nohands :oviparous :poisonous :hostile :female :prince})
  (MonsterType. "acid blob", \b, :green, 1, 3, 8, 0, 0, #{:genocidable}, [(MonsterAttack. :passive :acid 1 8)], 30, 10, :silent, :tiny, #{:sleep :poison :acid :stone}, #{}, #{:breathless :amorphous :noeyes :nolimbs :nohead :mindless :acid :wander :neuter})
  (MonsterType. "quivering blob", \b, :white, 5, 1, 8, 0, 0, #{:genocidable}, [(MonsterAttack. :touch :physical 1 8)], 200, 100, :silent, :small, #{:sleep :poison}, #{:poison}, #{:noeyes :nolimbs :nohead :mindless :wander :hostile :neuter})
  (MonsterType. "gelatinous cube", \b, :cyan, 6, 6, 8, 0, 0, #{:genocidable}, [(MonsterAttack. :touch :paralysis 2 4) (MonsterAttack. :passive :paralysis 1 4)], 600, 150, :silent, :large, #{:fire :cold :shock :sleep :poison :acid :stone}, #{:fire :cold :shock :sleep}, #{:noeyes :nolimbs :nohead :mindless :omnivore :acid :wander :hostile :neuter})
  (MonsterType. "chickatrice", \c, :brown, 4, 4, 8, 30, 0, #{:genocidable :sgroup}, [(MonsterAttack. :bite :physical 1 2) (MonsterAttack. :touch :stone 0 0) (MonsterAttack. :passive :stone 0 0)], 10, 10, :hiss, :tiny, #{:poison :stone}, #{:poison}, #{:animal :nohands :omnivore :hostile :infravisible})
  (MonsterType. "cockatrice", \c, :yellow, 5, 6, 6, 30, 0, #{:genocidable}, [(MonsterAttack. :bite :physical 1 3) (MonsterAttack. :touch :stone 0 0) (MonsterAttack. :passive :stone 0 0)], 30, 30, :hiss, :small, #{:poison :stone}, #{:poison}, #{:animal :nohands :omnivore :oviparous :hostile :infravisible})
  (MonsterType. "pyrolisk", \c, :red, 6, 6, 6, 30, 0, #{:genocidable}, [(MonsterAttack. :gaze :fire 2 6)], 30, 30, :hiss, :small, #{:poison :fire}, #{:poison :fire}, #{:animal :nohands :omnivore :oviparous :hostile :infravisible})
  (MonsterType. "jackal", \d, :brown, 0, 12, 7, 0, 0, #{:genocidable :sgroup}, [(MonsterAttack. :bite :physical 1 2)], 300, 250, :bark, :small, #{}, #{}, #{:animal :nohands :carnivore :hostile :infravisible})
  (MonsterType. "fox", \d, :red, 0, 15, 7, 0, 0, #{:genocidable}, [(MonsterAttack. :bite :physical 1 3)], 300, 250, :bark, :small, #{}, #{}, #{:animal :nohands :carnivore :hostile :infravisible})
  (MonsterType. "coyote", \d, :brown, 1, 12, 7, 0, 0, #{:genocidable :sgroup}, [(MonsterAttack. :bite :physical 1 4)], 300, 250, :bark, :small, #{}, #{}, #{:animal :nohands :carnivore :hostile :infravisible})
  (MonsterType. "werejackal", \d, :brown, 2, 12, 7, 10, -7, #{:not-generated :no-corpse}, [(MonsterAttack. :bite :lycantrophy 1 4)], 300, 250, :bark, :small, #{:poison}, #{}, #{:nohands :poisonous :regen :carnivore :nopoly :were :hostile :infravisible})
  (MonsterType. "little dog", \d, :white, 2, 18, 6, 0, 0, #{:genocidable}, [(MonsterAttack. :bite :physical 1 6)], 150, 150, :bark, :small, #{}, #{}, #{:animal :nohands :carnivore :domestic :infravisible})
  (MonsterType. "dog", \d, :white, 4, 16, 5, 0, 0, #{:genocidable}, [(MonsterAttack. :bite :physical 1 6)], 400, 200, :bark, :medium, #{}, #{}, #{:animal :nohands :carnivore :domestic :infravisible})
  (MonsterType. "large dog", \d, :white, 6, 15, 4, 0, 0, #{:genocidable}, [(MonsterAttack. :bite :physical 2 4)], 800, 250, :bark, :medium, #{}, #{}, #{:animal :nohands :carnivore :strong :domestic :infravisible})
  (MonsterType. "dingo", \d, :yellow, 4, 16, 5, 0, 0, #{:genocidable}, [(MonsterAttack. :bite :physical 1 6)], 400, 200, :bark, :medium, #{}, #{}, #{:animal :nohands :carnivore :hostile :infravisible})
  (MonsterType. "wolf", \d, :brown, 5, 12, 4, 0, 0, #{:genocidable :sgroup}, [(MonsterAttack. :bite :physical 2 4)], 500, 250, :bark, :medium, #{}, #{}, #{:animal :nohands :carnivore :hostile :infravisible})
  (MonsterType. "werewolf", \d, :brown, 5, 12, 4, 20, -7, #{:not-generated :no-corpse}, [(MonsterAttack. :bite :lycantrophy 2 6)], 500, 250, :bark, :medium, #{:poison}, #{}, #{:nohands :poisonous :regen :carnivore :nopoly :were :hostile :infravisible})
  (MonsterType. "warg", \d, :brown, 7, 12, 4, 0, -5, #{:genocidable :sgroup}, [(MonsterAttack. :bite :physical 2 6)], 850, 350, :bark, :medium, #{}, #{}, #{:animal :nohands :carnivore :hostile :infravisible})
  (MonsterType. "winter wolf cub", \d, :cyan, 5, 12, 4, 0, -5, #{:no-hell :genocidable :sgroup}, [(MonsterAttack. :bite :physical 1 8) (MonsterAttack. :breath :cold 1 8)], 250, 200, :bark, :small, #{:cold}, #{:cold}, #{:animal :nohands :carnivore :hostile})
  (MonsterType. "winter wolf", \d, :cyan, 7, 12, 4, 20, 0, #{:no-hell :genocidable}, [(MonsterAttack. :bite :physical 2 6) (MonsterAttack. :breath :cold 2 6)], 700, 300, :bark, :large, #{:cold}, #{:cold}, #{:animal :nohands :carnivore :hostile :strong})
  (MonsterType. "hell hound pup", \d, :red, 7, 12, 4, 20, -5, #{:hell-only :genocidable :sgroup}, [(MonsterAttack. :bite :physical 2 6) (MonsterAttack. :breath :fire 2 6)], 200, 200, :bark, :small, #{:fire}, #{:fire}, #{:animal :nohands :carnivore :hostile :infravisible})
  (MonsterType. "hell hound", \d, :red, 12, 14, 2, 20, 0, #{:hell-only :genocidable}, [(MonsterAttack. :bite :physical 3 6) (MonsterAttack. :breath :fire 3 6)], 600, 300, :bark, :medium, #{:fire}, #{:fire}, #{:animal :nohands :carnivore :hostile :strong :infravisible})
  (MonsterType. "gas spore", \e, nil, 1, 3, 10, 0, 0, #{:no-corpse :genocidable}, [(MonsterAttack. :boom :physical 4 6)], 10, 10, :silent, :small, #{}, #{}, #{:fly :breathless :nolimbs :nohead :mindless :hostile :neuter})
  (MonsterType. "floating eye", \e, :blue, 2, 1, 9, 10, 0, #{:genocidable}, [(MonsterAttack. :passive :paralysis 0 70)], 10, 10, :silent, :small, #{}, #{:telepathy}, #{:telepathic :fly :amphibious :nolimbs :nohead :notake :hostile :neuter :infravisible})
  (MonsterType. "freezing sphere", \e, :white, 6, 13, 4, 0, 0, #{:no-corpse :no-hell :genocidable}, [(MonsterAttack. :explode :cold 4 6)], 10, 10, :silent, :small, #{:cold}, #{:cold}, #{:fly :breathless :nolimbs :nohead :mindless :notake :hostile :neuter :infravisible})
  (MonsterType. "flaming sphere", \e, :red, 6, 13, 4, 0, 0, #{:no-corpse :genocidable}, [(MonsterAttack. :explode :fire 4 6)], 10, 10, :silent, :small, #{:fire}, #{:fire}, #{:fly :breathless :nolimbs :nohead :mindless :hostile :neuter :infravisible})
  (MonsterType. "shocking sphere", \e, :bright-blue, 6, 13, 4, 0, 0, #{:no-corpse :genocidable}, [(MonsterAttack. :explode :shock 4 6)], 10, 10, :silent, :small, #{:shock}, #{:shock}, #{:fly :breathless :nolimbs :nohead :mindless :hostile :neuter :infravisible})
  (MonsterType. "kitten", \f, :white, 2, 18, 6, 0, 0, #{:genocidable}, [(MonsterAttack. :bite :physical 1 6)], 150, 150, :mew, :small, #{}, #{}, #{:animal :nohands :carnivore :wander :domestic :infravisible})
  (MonsterType. "housecat", \f, :white, 4, 16, 5, 0, 0, #{:genocidable}, [(MonsterAttack. :bite :physical 1 6)], 200, 200, :mew, :small, #{}, #{}, #{:animal :nohands :carnivore :domestic :infravisible})
  (MonsterType. "jaguar", \f, :brown, 4, 15, 6, 0, 0, #{:genocidable}, [(MonsterAttack. :claw :physical 1 4) (MonsterAttack. :claw :physical 1 4) (MonsterAttack. :bite :physical 1 8)], 600, 300, :growl, :large, #{}, #{}, #{:animal :nohands :carnivore :hostile :infravisible})
  (MonsterType. "lynx", \f, :cyan, 5, 15, 6, 0, 0, #{:genocidable}, [(MonsterAttack. :claw :physical 1 4) (MonsterAttack. :claw :physical 1 4) (MonsterAttack. :bite :physical 1 10)], 600, 300, :growl, :small, #{}, #{}, #{:animal :nohands :carnivore :hostile :infravisible})
  (MonsterType. "panther", \f, :blue, 5, 15, 6, 0, 0, #{:genocidable}, [(MonsterAttack. :claw :physical 1 6) (MonsterAttack. :claw :physical 1 6) (MonsterAttack. :bite :physical 1 10)], 600, 300, :growl, :large, #{}, #{}, #{:animal :nohands :carnivore :hostile :infravisible})
  (MonsterType. "large cat", \f, :white, 6, 15, 4, 0, 0, #{:genocidable}, [(MonsterAttack. :bite :physical 2 4)], 250, 250, :mew, :small, #{}, #{}, #{:animal :nohands :carnivore :strong :domestic :infravisible})
  (MonsterType. "tiger", \f, :yellow, 6, 12, 6, 0, 0, #{:genocidable}, [(MonsterAttack. :claw :physical 2 4) (MonsterAttack. :claw :physical 2 4) (MonsterAttack. :bite :physical 1 10)], 600, 300, :growl, :large, #{}, #{}, #{:animal :nohands :carnivore :hostile :infravisible})
  (MonsterType. "winged gargoyle", \g, :magenta, 9, 15, -2, 0, -12, #{:genocidable}, [(MonsterAttack. :claw :physical 3 6) (MonsterAttack. :claw :physical 3 6) (MonsterAttack. :bite :physical 3 4)], 1200, 300, :grunt, :human, #{:stone}, #{}, #{:fly :humanoid :thick-hide :breathless :oviparous :lord :hostile :strong :picks-magic-items})
  (MonsterType. "hobbit", \h, :green, 1, 9, 10, 0, 6, #{:genocidable}, [(MonsterAttack. :weapon :physical 1 6)], 500, 200, :humanoid, :small, #{}, #{}, #{:humanoid :omnivore :collect :infravisible :infravision})
  (MonsterType. "dwarf", \h, :red, 2, 6, 10, 10, 4, #{:genocidable}, [(MonsterAttack. :weapon :physical 1 8)], 900, 300, :humanoid, :human, #{}, #{}, #{:tunnel :digger :humanoid :omnivore :nopoly :dwarf :strong :picks-gold :picks-jewels :collect :infravisible :infravision})
  (MonsterType. "bugbear", \h, :brown, 3, 9, 5, 0, -6, #{:genocidable}, [(MonsterAttack. :weapon :physical 2 4)], 1250, 250, :growl, :large, #{}, #{}, #{:humanoid :omnivore :strong :collect :infravisible :infravision})
  (MonsterType. "dwarf lord", \h, :blue, 4, 6, 10, 10, 5, #{:genocidable}, [(MonsterAttack. :weapon :physical 2 4) (MonsterAttack. :weapon :physical 2 4)], 900, 300, :humanoid, :human, #{}, #{}, #{:tunnel :digger :humanoid :omnivore :dwarf :strong :lord :male :picks-gold :picks-jewels :collect :infravisible :infravision})
  (MonsterType. "dwarf king", \h, :magenta, 6, 6, 10, 20, 6, #{:genocidable}, [(MonsterAttack. :weapon :physical 2 6) (MonsterAttack. :weapon :physical 2 6)], 900, 300, :humanoid, :human, #{}, #{}, #{:tunnel :digger :humanoid :omnivore :dwarf :strong :prince :male :picks-gold :picks-jewels :collect :infravisible :infravision})
  (MonsterType. "mind flayer", \h, :magenta, 9, 12, 5, 90, -8, #{:genocidable}, [(MonsterAttack. :weapon :physical 1 4) (MonsterAttack. :tentacle :drain-int 2 1) (MonsterAttack. :tentacle :drain-int 2 1) (MonsterAttack. :tentacle :drain-int 2 1)], 1450, 400, :hiss, :human, #{}, #{:telepathy}, #{:telepathic :humanoid :fly :see-invis :omnivore :hostile :nasty :picks-gold :picks-jewels :collect :infravisible :infravision})
  (MonsterType. "master mind flayer", \h, :magenta, 13, 12, 0, 90, -8, #{:genocidable}, [(MonsterAttack. :weapon :physical 1 8) (MonsterAttack. :tentacle :drain-int 2 1) (MonsterAttack. :tentacle :drain-int 2 1) (MonsterAttack. :tentacle :drain-int 2 1) (MonsterAttack. :tentacle :drain-int 2 1) (MonsterAttack. :tentacle :drain-int 2 1)], 1450, 400, :hiss, :human, #{}, #{:telepathy}, #{:telepathic :humanoid :fly :see-invis :omnivore :hostile :nasty :picks-gold :picks-jewels :collect :infravisible :infravision})
  (MonsterType. "manes", \i, :red, 1, 3, 7, 0, -7, #{:genocidable :lgroup :no-corpse}, [(MonsterAttack. :claw :physical 1 3) (MonsterAttack. :claw :physical 1 3) (MonsterAttack. :bite :physical 1 4)], 100, 100, :silent, :small, #{:sleep :poison}, #{}, #{:poisonous :hostile :follows :infravisible :infravision})
  (MonsterType. "homunculus", \i, :green, 2, 12, 6, 10, -7, #{:genocidable}, [(MonsterAttack. :bite :sleep 1 3)], 60, 100, :silent, :tiny, #{:sleep :poison}, #{:sleep :poison}, #{:fly :poisonous :follows :infravisible :infravision})
  (MonsterType. "imp", \i, :red, 3, 12, 2, 20, -7, #{:genocidable}, [(MonsterAttack. :claw :physical 1 4)], 20, 10, :cuss, :tiny, #{}, #{}, #{:regen :wander :follows :infravisible :infravision})
  (MonsterType. "lemure", \i, :brown, 3, 3, 7, 0, -7, #{:hell-only :genocidable :lgroup :no-corpse}, [(MonsterAttack. :claw :physical 1 3)], 150, 100, :silent, :medium, #{:sleep :poison}, #{:sleep}, #{:poisonous :regen :hostile :wander :follows :neuter :infravisible :infravision})
  (MonsterType. "quasit", \i, :blue, 3, 15, 2, 20, -7, #{:genocidable}, [(MonsterAttack. :claw :drain-dex 1 2) (MonsterAttack. :claw :drain-dex 1 2) (MonsterAttack. :bite :physical 1 4)], 200, 200, :silent, :small, #{:poison}, #{:poison}, #{:regen :follows :infravisible :infravision})
  (MonsterType. "tengu", \i, :cyan, 6, 13, 5, 30, 7, #{:genocidable}, [(MonsterAttack. :bite :physical 1 7)], 300, 200, :sqawk, :small, #{:poison}, #{:poison}, #{:teleport :telecontrol :follows :infravisible :infravision})
  (MonsterType. "blue jelly", \j, :blue, 4, 0, 8, 10, 0, #{:genocidable}, [(MonsterAttack. :passive :cold 0 6)], 50, 20, :silent, :medium, #{:cold :poison}, #{:cold :poison}, #{:breathless :amorphous :noeyes :nolimbs :nohead :mindless :notake :hostile :neuter :sessile})
  (MonsterType. "spotted jelly", \j, :green, 5, 0, 8, 10, 0, #{:genocidable}, [(MonsterAttack. :passive :acid 0 6)], 50, 20, :silent, :medium, #{:acid :stone}, #{}, #{:breathless :amorphous :noeyes :nolimbs :nohead :mindless :acid :notake :hostile :neuter :sessile})
  (MonsterType. "ochre jelly", \j, :brown, 6, 3, 8, 20, 0, #{:genocidable}, [(MonsterAttack. :engulf :acid 3 6) (MonsterAttack. :passive :acid 3 6)], 50, 20, :silent, :medium, #{:acid :stone}, #{}, #{:breathless :amorphous :noeyes :nolimbs :nohead :mindless :acid :notake :hostile :neuter})
  (MonsterType. "kobold", \k, :brown, 0, 6, 10, 0, -2, #{:genocidable}, [(MonsterAttack. :weapon :physical 1 4)], 400, 100, :grunt, :small, #{:poison}, #{}, #{:humanoid :poisonous :omnivore :hostile :collect :infravisible :infravision})
  (MonsterType. "large kobold", \k, :red, 1, 6, 10, 0, -3, #{:genocidable}, [(MonsterAttack. :weapon :physical 1 6)], 450, 150, :grunt, :small, #{:poison}, #{}, #{:humanoid :poisonous :omnivore :hostile :collect :infravisible :infravision})
  (MonsterType. "kobold lord", \k, :magenta, 2, 6, 10, 0, -4, #{:genocidable}, [(MonsterAttack. :weapon :physical 2 4)], 500, 200, :grunt, :small, #{:poison}, #{}, #{:humanoid :poisonous :omnivore :hostile :lord :male :collect :infravisible :infravision})
  (MonsterType. "kobold shaman", \k, :bright-blue, 2, 6, 6, 10, -4, #{:genocidable}, [(MonsterAttack. :magic :spell 0 0)], 450, 150, :grunt, :small, #{:poison}, #{}, #{:humanoid :poisonous :omnivore :hostile :picks-magic-items :infravisible :infravision})
  (MonsterType. "leprechaun", \l, :green, 5, 15, 8, 20, 0, #{:genocidable}, [(MonsterAttack. :claw :steal-gold 1 2)], 60, 30, :laugh, :tiny, #{}, #{}, #{:humanoid :teleport :hostile :picks-gold :infravisible})
  (MonsterType. "small mimic", \m, :brown, 7, 3, 7, 0, 0, #{:genocidable}, [(MonsterAttack. :claw :physical 3 4)], 300, 200, :silent, :medium, #{:acid}, #{}, #{:breathless :amorphous :hide :animal :noeyes :nohead :nolimbs :thick-hide :carnivore :hostile})
  (MonsterType. "large mimic", \m, :red, 8, 3, 7, 10, 0, #{:genocidable}, [(MonsterAttack. :claw :stick 3 4)], 600, 400, :silent, :large, #{:acid}, #{}, #{:cling :breathless :amorphous :hide :animal :noeyes :nohead :nolimbs :thick-hide :carnivore :hostile :strong})
  (MonsterType. "giant mimic", \m, :magenta, 9, 3, 7, 20, 0, #{:genocidable}, [(MonsterAttack. :claw :stick 3 6) (MonsterAttack. :claw :stick 3 6)], 800, 500, :silent, :large, #{:acid}, #{}, #{:cling :breathless :amorphous :hide :animal :noeyes :nohead :nolimbs :thick-hide :carnivore :hostile :strong})
  (MonsterType. "wood nymph", \n, :green, 3, 12, 9, 20, 0, #{:genocidable}, [(MonsterAttack. :claw :steal-items 0 0) (MonsterAttack. :claw :seduce 0 0)], 600, 300, :seduce, :human, #{}, #{}, #{:humanoid :teleport :hostile :female :collect :infravisible})
  (MonsterType. "water nymph", \n, :blue, 3, 12, 9, 20, 0, #{:genocidable}, [(MonsterAttack. :claw :steal-items 0 0) (MonsterAttack. :claw :seduce 0 0)], 600, 300, :seduce, :human, #{}, #{}, #{:humanoid :teleport :swim :hostile :female :collect :infravisible})
  (MonsterType. "mountain nymph", \n, :brown, 3, 12, 9, 20, 0, #{:genocidable}, [(MonsterAttack. :claw :steal-items 0 0) (MonsterAttack. :claw :seduce 0 0)], 600, 300, :seduce, :human, #{}, #{}, #{:humanoid :teleport :hostile :female :collect :infravisible})
  (MonsterType. "goblin", \o, nil, 0, 6, 10, 0, -3, #{:genocidable}, [(MonsterAttack. :weapon :physical 1 4)], 400, 100, :grunt, :small, #{}, #{}, #{:humanoid :omnivore :orc :collect :infravisible :infravision})
  (MonsterType. "hobgoblin", \o, :brown, 1, 9, 10, 0, -4, #{:genocidable}, [(MonsterAttack. :weapon :physical 1 6)], 1000, 200, :grunt, :human, #{}, #{}, #{:humanoid :omnivore :orc :strong :collect :infravisible :infravision})
  (MonsterType. "orc", \o, :red, 1, 9, 10, 0, -3, #{:genocidable :not-generated :lgroup}, [(MonsterAttack. :weapon :physical 1 8)], 850, 150, :grunt, :human, #{}, #{}, #{:humanoid :omnivore :nopoly :orc :strong :picks-gold :picks-jewels :collect :infravisible :infravision})
  (MonsterType. "hill orc", \o, :yellow, 2, 9, 10, 0, -4, #{:genocidable :lgroup}, [(MonsterAttack. :weapon :physical 1 6)], 1000, 200, :grunt, :human, #{}, #{}, #{:humanoid :omnivore :orc :strong :picks-gold :picks-jewels :collect :infravisible :infravision})
  (MonsterType. "Mordor orc", \o, :blue, 3, 5, 10, 0, -5, #{:genocidable :lgroup}, [(MonsterAttack. :weapon :physical 1 6)], 1200, 200, :grunt, :human, #{}, #{}, #{:humanoid :omnivore :orc :strong :picks-gold :picks-jewels :collect :infravisible :infravision})
  (MonsterType. "Uruk-hai", \o, :blue, 3, 7, 10, 0, -4, #{:genocidable :lgroup}, [(MonsterAttack. :weapon :physical 1 8)], 1300, 300, :grunt, :human, #{}, #{}, #{:humanoid :omnivore :orc :strong :picks-gold :picks-jewels :collect :infravisible :infravision})
  (MonsterType. "orc shaman", \o, :bright-blue, 3, 9, 5, 10, -5, #{:genocidable}, [(MonsterAttack. :magic :spell 0 0)], 1000, 300, :grunt, :human, #{}, #{}, #{:humanoid :omnivore :orc :strong :picks-gold :picks-jewels :picks-magic-items :infravisible :infravision})
  (MonsterType. "orc-captain", \o, :magenta, 5, 5, 10, 0, -5, #{:genocidable}, [(MonsterAttack. :weapon :physical 2 4) (MonsterAttack. :weapon :physical 2 4)], 1350, 350, :grunt, :human, #{}, #{}, #{:humanoid :omnivore :orc :strong :picks-gold :picks-jewels :collect :infravisible :infravision})
  (MonsterType. "rock piercer", \p, nil, 3, 1, 3, 0, 0, #{:genocidable}, [(MonsterAttack. :bite :physical 2 6)], 200, 200, :silent, :small, #{}, #{}, #{:cling :hide :animal :noeyes :nolimbs :carnivore :notake :hostile})
  (MonsterType. "iron piercer", \p, :cyan, 5, 1, 0, 0, 0, #{:genocidable}, [(MonsterAttack. :bite :physical 3 6)], 400, 300, :silent, :medium, #{}, #{}, #{:cling :hide :animal :noeyes :nolimbs :carnivore :notake :hostile})
  (MonsterType. "glass piercer", \p, :white, 7, 1, 0, 0, 0, #{:genocidable}, [(MonsterAttack. :bite :physical 4 6)], 400, 300, :silent, :medium, #{:acid}, #{}, #{:cling :hide :animal :noeyes :nolimbs :carnivore :notake :hostile})
  (MonsterType. "rothe", \q, :brown, 2, 9, 7, 0, 0, #{:genocidable :sgroup}, [(MonsterAttack. :claw :physical 1 3) (MonsterAttack. :bite :physical 1 3) (MonsterAttack. :bite :physical 1 8)], 400, 100, :silent, :large, #{}, #{}, #{:animal :nohands :omnivore :hostile :infravisible})
  (MonsterType. "mumak", \q, nil, 5, 9, 0, 0, -2, #{:genocidable}, [(MonsterAttack. :butt :physical 4 12) (MonsterAttack. :bite :physical 2 6)], 2500, 500, :roar, :large, #{}, #{}, #{:animal :thick-hide :nohands :herbivore :hostile :strong :infravisible})
  (MonsterType. "leocrotta", \q, :red, 6, 18, 4, 10, 0, #{:genocidable}, [(MonsterAttack. :claw :physical 2 6) (MonsterAttack. :bite :physical 2 6) (MonsterAttack. :claw :physical 2 6)], 1200, 500, :imitate, :large, #{}, #{}, #{:animal :nohands :omnivore :hostile :strong :infravisible})
  (MonsterType. "wumpus", \q, :cyan, 8, 3, 2, 10, 0, #{:genocidable}, [(MonsterAttack. :bite :physical 3 6)], 2500, 500, :burble, :large, #{}, #{}, #{:cling :animal :nohands :omnivore :hostile :strong :infravisible})
  (MonsterType. "titanothere", \q, nil, 12, 12, 6, 0, 0, #{:genocidable}, [(MonsterAttack. :claw :physical 2 8)], 2650, 650, :silent, :large, #{}, #{}, #{:animal :thick-hide :nohands :herbivore :hostile :strong :infravisible})
  (MonsterType. "baluchitherium", \q, nil, 14, 12, 5, 0, 0, #{:genocidable}, [(MonsterAttack. :claw :physical 5 4) (MonsterAttack. :claw :physical 5 4)], 3800, 800, :silent, :large, #{}, #{}, #{:animal :thick-hide :nohands :herbivore :hostile :strong :infravisible})
  (MonsterType. "mastodon", \q, :blue, 20, 12, 5, 0, 0, #{:genocidable}, [(MonsterAttack. :butt :physical 4 8) (MonsterAttack. :butt :physical 4 8)], 3800, 800, :silent, :large, #{}, #{}, #{:animal :thick-hide :nohands :herbivore :hostile :strong :infravisible})
  (MonsterType. "sewer rat", \r, :brown, 0, 12, 7, 0, 0, #{:genocidable :sgroup}, [(MonsterAttack. :bite :physical 1 3)], 20, 12, :sqeek, :tiny, #{}, #{}, #{:animal :nohands :carnivore :hostile :infravisible})
  (MonsterType. "giant rat", \r, :brown, 1, 10, 7, 0, 0, #{:genocidable :sgroup}, [(MonsterAttack. :bite :physical 1 3)], 30, 30, :sqeek, :tiny, #{}, #{}, #{:animal :nohands :carnivore :hostile :infravisible})
  (MonsterType. "rabid rat", \r, :brown, 2, 12, 6, 0, 0, #{:genocidable}, [(MonsterAttack. :bite :drain-con 2 4)], 30, 5, :sqeek, :tiny, #{:poison}, #{}, #{:animal :nohands :poisonous :carnivore :hostile :infravisible})
  (MonsterType. "wererat", \r, :brown, 2, 12, 6, 10, -7, #{:not-generated :no-corpse}, [(MonsterAttack. :bite :lycantrophy 1 4)], 40, 30, :sqeek, :tiny, #{:poison}, #{}, #{:nohands :poisonous :regen :carnivore :nopoly :were :hostile :infravisible})
  (MonsterType. "rock mole", \r, nil, 3, 3, 0, 20, 0, #{:genocidable}, [(MonsterAttack. :bite :physical 1 6)], 30, 30, :silent, :small, #{}, #{}, #{:tunnel :animal :nohands :metallivore :hostile :picks-gold :picks-jewels :collect :infravisible})
  (MonsterType. "woodchuck", \r, :brown, 3, 3, 0, 20, 0, #{:not-generated :genocidable}, [(MonsterAttack. :bite :physical 1 6)], 30, 30, :silent, :small, #{}, #{}, #{:tunnel :animal :nohands :swim :herbivore :wander :hostile :infravisible})
  (MonsterType. "cave spider", \s, nil, 1, 12, 3, 0, 0, #{:genocidable :sgroup}, [(MonsterAttack. :bite :physical 1 2)], 50, 50, :silent, :tiny, #{:poison}, #{:poison}, #{:conceal :animal :nohands :oviparous :carnivore :hostile})
  (MonsterType. "centipede", \s, :yellow, 2, 4, 3, 0, 0, #{:genocidable}, [(MonsterAttack. :bite :poison 1 3)], 50, 50, :silent, :tiny, #{:poison}, #{:poison}, #{:conceal :animal :nohands :oviparous :carnivore :hostile})
  (MonsterType. "giant spider", \s, :magenta, 5, 15, 4, 0, 0, #{:genocidable}, [(MonsterAttack. :bite :poison 2 4)], 100, 100, :silent, :large, #{:poison}, #{:poison}, #{:animal :nohands :oviparous :poisonous :carnivore :hostile :strong})
  (MonsterType. "scorpion", \s, :red, 5, 15, 3, 0, 0, #{:genocidable}, [(MonsterAttack. :claw :physical 1 2) (MonsterAttack. :claw :physical 1 2) (MonsterAttack. :sting :poison 1 4)], 50, 100, :silent, :small, #{:poison}, #{:poison}, #{:conceal :animal :nohands :oviparous :poisonous :carnivore :hostile})
  (MonsterType. "lurker above", \t, nil, 10, 3, 3, 0, 0, #{:genocidable}, [(MonsterAttack. :engulf :digest 1 8)], 800, 350, :silent, :huge, #{}, #{}, #{:hide :fly :animal :noeyes :nolimbs :nohead :carnivore :hostile :follows :strong})
  (MonsterType. "trapper", \t, :green, 12, 3, 3, 0, 0, #{:genocidable}, [(MonsterAttack. :engulf :digest 1 10)], 800, 350, :silent, :huge, #{}, #{}, #{:hide :animal :noeyes :nolimbs :nohead :carnivore :hostile :follows :strong})
  (MonsterType. "white unicorn", \u, :white, 4, 24, 2, 70, 7, #{:genocidable}, [(MonsterAttack. :butt :physical 1 12) (MonsterAttack. :kick :physical 1 6)], 1300, 300, :neigh, :large, #{:poison}, #{:poison}, #{:nohands :herbivore :wander :strong :picks-jewels :infravisible})
  (MonsterType. "gray unicorn", \u, nil, 4, 24, 2, 70, 0, #{:genocidable}, [(MonsterAttack. :butt :physical 1 12) (MonsterAttack. :kick :physical 1 6)], 1300, 300, :neigh, :large, #{:poison}, #{:poison}, #{:nohands :herbivore :wander :strong :picks-jewels :infravisible})
  (MonsterType. "black unicorn", \u, :blue, 4, 24, 2, 70, -7, #{:genocidable}, [(MonsterAttack. :butt :physical 1 12) (MonsterAttack. :kick :physical 1 6)], 1300, 300, :neigh, :large, #{:poison}, #{:poison}, #{:nohands :herbivore :wander :strong :picks-jewels :infravisible})
  (MonsterType. "pony", \u, :brown, 3, 16, 6, 0, 0, #{:genocidable}, [(MonsterAttack. :kick :physical 1 6) (MonsterAttack. :bite :physical 1 2)], 1300, 250, :neigh, :medium, #{}, #{}, #{:animal :nohands :herbivore :wander :strong :domestic :infravisible})
  (MonsterType. "horse", \u, :brown, 5, 20, 5, 0, 0, #{:genocidable}, [(MonsterAttack. :kick :physical 1 8) (MonsterAttack. :bite :physical 1 3)], 1500, 300, :neigh, :large, #{}, #{}, #{:animal :nohands :herbivore :wander :strong :domestic :infravisible})
  (MonsterType. "warhorse", \u, :brown, 7, 24, 4, 0, 0, #{:genocidable}, [(MonsterAttack. :kick :physical 1 10) (MonsterAttack. :bite :physical 1 4)], 1800, 350, :neigh, :large, #{}, #{}, #{:animal :nohands :herbivore :wander :strong :domestic :infravisible})
  (MonsterType. "fog cloud", \v, nil, 3, 1, 0, 0, 0, #{:genocidable :no-corpse}, [(MonsterAttack. :engulf :physical 1 6)], 0, 0, :silent, :huge, #{:sleep :poison :stone}, #{}, #{:fly :breathless :noeyes :nolimbs :nohead :mindless :amorphous :unsolid :hostile :neuter})
  (MonsterType. "dust vortex", \v, :brown, 4, 20, 2, 30, 0, #{:genocidable :no-corpse}, [(MonsterAttack. :engulf :blinding 2 8)], 0, 0, :silent, :huge, #{:sleep :poison :stone}, #{}, #{:fly :breathless :noeyes :nolimbs :nohead :mindless :hostile :neuter})
  (MonsterType. "ice vortex", \v, :cyan, 5, 20, 2, 30, 0, #{:no-hell :genocidable :no-corpse}, [(MonsterAttack. :engulf :cold 1 6)], 0, 0, :silent, :huge, #{:cold :sleep :poison :stone}, #{}, #{:fly :breathless :noeyes :nolimbs :nohead :mindless :hostile :neuter :infravisible})
  (MonsterType. "energy vortex", \v, :bright-blue, 6, 20, 2, 30, 0, #{:genocidable :no-corpse}, [(MonsterAttack. :engulf :shock 1 6) (MonsterAttack. :engulf :drain-magic 0 0) (MonsterAttack. :passive :shock 0 4)], 0, 0, :silent, :huge, #{:shock :sleep :disintegration :poison :stone}, #{}, #{:fly :breathless :noeyes :nolimbs :nohead :mindless :unsolid :hostile :neuter})
  (MonsterType. "steam vortex", \v, :blue, 7, 22, 2, 30, 0, #{:hell-only :genocidable :no-corpse}, [(MonsterAttack. :engulf :fire 1 8)], 0, 0, :silent, :huge, #{:fire :sleep :poison :stone}, #{}, #{:fly :breathless :noeyes :nolimbs :nohead :mindless :unsolid :hostile :neuter :infravisible})
  (MonsterType. "fire vortex", \v, :yellow, 8, 22, 2, 30, 0, #{:hell-only :genocidable :no-corpse}, [(MonsterAttack. :engulf :fire 1 10) (MonsterAttack. :passive :fire 0 4)], 0, 0, :silent, :huge, #{:fire :sleep :poison :stone}, #{}, #{:fly :breathless :noeyes :nolimbs :nohead :mindless :unsolid :hostile :neuter :infravisible})
  (MonsterType. "baby long worm", \w, :brown, 8, 3, 5, 0, 0, #{:genocidable}, [(MonsterAttack. :bite :physical 1 6)], 600, 250, :silent, :large, #{}, #{}, #{:animal :slithy :nolimbs :carnivore :notake :hostile})
  (MonsterType. "baby purple worm", \w, :magenta, 8, 3, 5, 0, 0, #{:genocidable}, [(MonsterAttack. :bite :physical 1 6)], 600, 250, :silent, :large, #{}, #{}, #{:animal :slithy :nolimbs :carnivore :hostile})
  (MonsterType. "long worm", \w, :brown, 8, 3, 5, 10, 0, #{:genocidable}, [(MonsterAttack. :bite :physical 1 4)], 1500, 500, :silent, :gigantic, #{}, #{}, #{:animal :slithy :nolimbs :oviparous :carnivore :notake :hostile :strong :nasty})
  (MonsterType. "purple worm", \w, :magenta, 15, 9, 6, 20, 0, #{:genocidable}, [(MonsterAttack. :bite :physical 2 8) (MonsterAttack. :engulf :digest 1 10)], 2700, 700, :silent, :gigantic, #{}, #{}, #{:animal :slithy :nolimbs :oviparous :carnivore :hostile :strong :nasty})
  (MonsterType. "grid bug", \x, :magenta, 0, 12, 9, 0, 0, #{:genocidable :sgroup :no-corpse}, [(MonsterAttack. :bite :shock 1 1)], 15, 10, :buzz, :tiny, #{:shock :poison}, #{}, #{:animal :hostile :infravisible})
  (MonsterType. "xan", \x, :red, 7, 18, -4, 0, 0, #{:genocidable}, [(MonsterAttack. :sting :leg-hurt 1 4)], 300, 300, :buzz, :tiny, #{:poison}, #{:poison}, #{:fly :animal :nohands :poisonous :hostile :infravisible})
  (MonsterType. "yellow light", \y, :yellow, 3, 15, 0, 0, 0, #{:no-corpse :genocidable}, [(MonsterAttack. :explode :blinding 10 20)], 0, 0, :silent, :small, #{:fire :cold :shock :disintegration :sleep :poison :acid :stone}, #{}, #{:fly :breathless :amorphous :noeyes :nolimbs :nohead :mindless :unsolid :notake :hostile :neuter :infravisible})
  (MonsterType. "black light", \y, :blue, 5, 15, 0, 0, 0, #{:no-corpse :genocidable}, [(MonsterAttack. :explode :hallu 10 12)], 0, 0, :silent, :small, #{:fire :cold :shock :disintegration :sleep :poison :acid :stone}, #{}, #{:fly :breathless :amorphous :noeyes :nolimbs :nohead :mindless :unsolid :see-invis :notake :hostile :neuter})
  (MonsterType. "zruty", \z, :brown, 9, 8, 3, 0, 0, #{:genocidable}, [(MonsterAttack. :claw :physical 3 4) (MonsterAttack. :claw :physical 3 4) (MonsterAttack. :bite :physical 3 6)], 1200, 600, :silent, :large, #{}, #{}, #{:animal :humanoid :carnivore :hostile :strong :infravisible})
  (MonsterType. "couatl", \A, :green, 8, 10, 5, 30, 7, #{:no-hell :sgroup :no-corpse}, [(MonsterAttack. :bite :poison 2 4) (MonsterAttack. :bite :physical 1 3) (MonsterAttack. :hug :wrap 2 4)], 900, 400, :hiss, :large, #{:elbereth :poison}, #{}, #{:fly :poisonous :minion :follows :strong :nasty :infravisible :infravision})
  (MonsterType. "Aleax", \A, :yellow, 10, 8, 0, 30, 7, #{:no-hell :no-corpse}, [(MonsterAttack. :weapon :physical 1 6) (MonsterAttack. :weapon :physical 1 6) (MonsterAttack. :kick :physical 1 4)], 1450, 400, :imitate, :human, #{:elbereth :cold :shock :sleep :poison}, #{}, #{:humanoid :see-invis :minion :follows :nasty :collect :infravisible :infravision})
  (MonsterType. "Angel", \A, :white, 14, 10, -4, 55, 12, #{:no-hell :no-corpse}, [(MonsterAttack. :weapon :physical 1 6) (MonsterAttack. :weapon :physical 1 6) (MonsterAttack. :claw :physical 1 4) (MonsterAttack. :magic :magic-missile 2 6)], 1450, 400, :cuss, :human, #{:elbereth :cold :shock :sleep :poison}, #{}, #{:fly :humanoid :see-invis :nopoly :minion :follows :strong :nasty :collect :infravisible :infravision})
  (MonsterType. "ki-rin", \A, :yellow, 16, 18, -5, 90, 15, #{:no-hell :no-corpse}, [(MonsterAttack. :kick :physical 2 4) (MonsterAttack. :kick :physical 2 4) (MonsterAttack. :butt :physical 3 6) (MonsterAttack. :magic :spell 2 6)], 1450, 400, :neigh, :large, #{:elbereth}, #{}, #{:fly :see-invis :nopoly :minion :follows :strong :nasty :lord :infravisible :infravision})
  (MonsterType. "Archon", \A, :magenta, 19, 16, -6, 80, 15, #{:no-hell :no-corpse}, [(MonsterAttack. :weapon :physical 2 4) (MonsterAttack. :weapon :physical 2 4) (MonsterAttack. :gaze :blinding 2 6) (MonsterAttack. :claw :physical 1 8) (MonsterAttack. :magic :spell 4 6)], 1450, 400, :cuss, :large, #{:elbereth :fire :cold :shock :sleep :poison}, #{}, #{:fly :humanoid :see-invis :regen :nopoly :minion :follows :strong :nasty :lord :collect :picks-magic-items :infravisible :infravision})
  (MonsterType. "bat", \B, :brown, 0, 22, 8, 0, 0, #{:genocidable :sgroup}, [(MonsterAttack. :bite :physical 1 4)], 20, 20, :sqeek, :tiny, #{}, #{}, #{:fly :animal :nohands :carnivore :wander :infravisible})
  (MonsterType. "giant bat", \B, :red, 2, 22, 7, 0, 0, #{:genocidable}, [(MonsterAttack. :bite :physical 1 6)], 30, 30, :sqeek, :small, #{}, #{}, #{:fly :animal :nohands :carnivore :wander :hostile :infravisible})
  (MonsterType. "raven", \B, :blue, 4, 20, 6, 0, 0, #{:genocidable}, [(MonsterAttack. :bite :physical 1 6) (MonsterAttack. :claw :blinding 1 6)], 40, 20, :sqawk, :small, #{}, #{}, #{:fly :animal :nohands :carnivore :wander :hostile :infravisible})
  (MonsterType. "vampire bat", \B, :blue, 5, 20, 6, 0, 0, #{:genocidable}, [(MonsterAttack. :bite :physical 1 6) (MonsterAttack. :bite :poison 0 0)], 30, 20, :sqeek, :small, #{:sleep :poison}, #{}, #{:fly :animal :nohands :poisonous :regen :omnivore :hostile :infravisible})
  (MonsterType. "plains centaur", \C, :brown, 4, 18, 4, 0, 0, #{:genocidable}, [(MonsterAttack. :weapon :physical 1 6) (MonsterAttack. :kick :physical 1 6)], 2500, 500, :humanoid, :large, #{}, #{}, #{:humanoid :omnivore :strong :picks-gold :collect :infravisible})
  (MonsterType. "forest centaur", \C, :green, 5, 18, 3, 10, -1, #{:genocidable}, [(MonsterAttack. :weapon :physical 1 8) (MonsterAttack. :kick :physical 1 6)], 2550, 600, :humanoid, :large, #{}, #{}, #{:humanoid :omnivore :strong :picks-gold :collect :infravisible})
  (MonsterType. "mountain centaur", \C, :cyan, 6, 20, 2, 10, -3, #{:genocidable}, [(MonsterAttack. :weapon :physical 1 10) (MonsterAttack. :kick :physical 1 6) (MonsterAttack. :kick :physical 1 6)], 2550, 500, :humanoid, :large, #{}, #{}, #{:humanoid :omnivore :strong :picks-gold :collect :infravisible})
  (MonsterType. "baby gray dragon", \D, nil, 12, 9, 2, 10, 0, #{:genocidable}, [(MonsterAttack. :bite :physical 2 6)], 1500, 500, :roar, :huge, #{}, #{}, #{:fly :thick-hide :nohands :carnivore :hostile :strong :picks-gold :picks-jewels})
  (MonsterType. "baby silver dragon", \D, :bright-cyan, 12, 9, 2, 10, 0, #{:genocidable}, [(MonsterAttack. :bite :physical 2 6)], 1500, 500, :roar, :huge, #{}, #{}, #{:fly :thick-hide :nohands :carnivore :hostile :strong :picks-gold :picks-jewels})
  (MonsterType. "baby red dragon", \D, :red, 12, 9, 2, 10, 0, #{:genocidable}, [(MonsterAttack. :bite :physical 2 6)], 1500, 500, :roar, :huge, #{:fire}, #{}, #{:fly :thick-hide :nohands :carnivore :hostile :strong :picks-gold :picks-jewels :infravisible})
  (MonsterType. "baby white dragon", \D, :white, 12, 9, 2, 10, 0, #{:genocidable}, [(MonsterAttack. :bite :physical 2 6)], 1500, 500, :roar, :huge, #{:cold}, #{}, #{:fly :thick-hide :nohands :carnivore :hostile :strong :picks-gold :picks-jewels})
  (MonsterType. "baby orange dragon", \D, :orange, 12, 9, 2, 10, 0, #{:genocidable}, [(MonsterAttack. :bite :physical 2 6)], 1500, 500, :roar, :huge, #{:sleep}, #{}, #{:fly :thick-hide :nohands :carnivore :hostile :strong :picks-gold :picks-jewels})
  (MonsterType. "baby black dragon", \D, :blue, 12, 9, 2, 10, 0, #{:genocidable}, [(MonsterAttack. :bite :physical 2 6)], 1500, 500, :roar, :huge, #{:disintegration}, #{}, #{:fly :thick-hide :nohands :carnivore :hostile :strong :picks-gold :picks-jewels})
  (MonsterType. "baby blue dragon", \D, :blue, 12, 9, 2, 10, 0, #{:genocidable}, [(MonsterAttack. :bite :physical 2 6)], 1500, 500, :roar, :huge, #{:shock}, #{}, #{:fly :thick-hide :nohands :carnivore :hostile :strong :picks-gold :picks-jewels})
  (MonsterType. "baby green dragon", \D, :green, 12, 9, 2, 10, 0, #{:genocidable}, [(MonsterAttack. :bite :physical 2 6)], 1500, 500, :roar, :huge, #{:poison}, #{}, #{:fly :thick-hide :nohands :carnivore :poisonous :hostile :strong :picks-gold :picks-jewels})
  (MonsterType. "baby yellow dragon", \D, :yellow, 12, 9, 2, 10, 0, #{:genocidable}, [(MonsterAttack. :bite :physical 2 6)], 1500, 500, :roar, :huge, #{:acid :stone}, #{}, #{:fly :thick-hide :nohands :carnivore :acid :hostile :strong :picks-gold :picks-jewels})
  (MonsterType. "gray dragon", \D, nil, 15, 9, -1, 20, 4, #{:genocidable}, [(MonsterAttack. :breath :magic-missile 4 6) (MonsterAttack. :bite :physical 3 8) (MonsterAttack. :claw :physical 1 4) (MonsterAttack. :claw :physical 1 4)], 4500, 1500, :roar, :gigantic, #{}, #{}, #{:fly :thick-hide :nohands :see-invis :oviparous :carnivore :hostile :strong :nasty :picks-gold :picks-jewels :picks-magic-items})
  (MonsterType. "silver dragon", \D, :bright-cyan, 15, 9, -1, 20, 4, #{:genocidable}, [(MonsterAttack. :breath :cold 4 6) (MonsterAttack. :bite :physical 3 8) (MonsterAttack. :claw :physical 1 4) (MonsterAttack. :claw :physical 1 4)], 4500, 1500, :roar, :gigantic, #{:cold}, #{}, #{:fly :thick-hide :nohands :see-invis :oviparous :carnivore :hostile :strong :nasty :picks-gold :picks-jewels :picks-magic-items})
  (MonsterType. "red dragon", \D, :red, 15, 9, -1, 20, -4, #{:genocidable}, [(MonsterAttack. :breath :fire 6 6) (MonsterAttack. :bite :physical 3 8) (MonsterAttack. :claw :physical 1 4) (MonsterAttack. :claw :physical 1 4)], 4500, 1500, :roar, :gigantic, #{:fire}, #{:fire}, #{:fly :thick-hide :nohands :see-invis :oviparous :carnivore :hostile :strong :nasty :picks-gold :picks-jewels :picks-magic-items :infravisible})
  (MonsterType. "white dragon", \D, :white, 15, 9, -1, 20, -5, #{:genocidable}, [(MonsterAttack. :breath :cold 4 6) (MonsterAttack. :bite :physical 3 8) (MonsterAttack. :claw :physical 1 4) (MonsterAttack. :claw :physical 1 4)], 4500, 1500, :roar, :gigantic, #{:cold}, #{:cold}, #{:fly :thick-hide :nohands :see-invis :oviparous :carnivore :hostile :strong :nasty :picks-gold :picks-jewels :picks-magic-items})
  (MonsterType. "orange dragon", \D, :orange, 15, 9, -1, 20, 5, #{:genocidable}, [(MonsterAttack. :breath :sleep 4 25) (MonsterAttack. :bite :physical 3 8) (MonsterAttack. :claw :physical 1 4) (MonsterAttack. :claw :physical 1 4)], 4500, 1500, :roar, :gigantic, #{:sleep}, #{:sleep}, #{:fly :thick-hide :nohands :see-invis :oviparous :carnivore :hostile :strong :nasty :picks-gold :picks-jewels :picks-magic-items})
  (MonsterType. "black dragon", \D, :blue, 15, 9, -1, 20, -6, #{:genocidable}, [(MonsterAttack. :breath :disintegration 4 10) (MonsterAttack. :bite :physical 3 8) (MonsterAttack. :claw :physical 1 4) (MonsterAttack. :claw :physical 1 4)], 4500, 1500, :roar, :gigantic, #{:disintegration}, #{:disintegration}, #{:fly :thick-hide :nohands :see-invis :oviparous :carnivore :hostile :strong :nasty :picks-gold :picks-jewels :picks-magic-items})
  (MonsterType. "blue dragon", \D, :blue, 15, 9, -1, 20, -7, #{:genocidable}, [(MonsterAttack. :breath :shock 4 6) (MonsterAttack. :bite :physical 3 8) (MonsterAttack. :claw :physical 1 4) (MonsterAttack. :claw :physical 1 4)], 4500, 1500, :roar, :gigantic, #{:shock}, #{:shock}, #{:fly :thick-hide :nohands :see-invis :oviparous :carnivore :hostile :strong :nasty :picks-gold :picks-jewels :picks-magic-items})
  (MonsterType. "green dragon", \D, :green, 15, 9, -1, 20, 6, #{:genocidable}, [(MonsterAttack. :breath :poison 4 6) (MonsterAttack. :bite :physical 3 8) (MonsterAttack. :claw :physical 1 4) (MonsterAttack. :claw :physical 1 4)], 4500, 1500, :roar, :gigantic, #{:poison}, #{:poison}, #{:fly :thick-hide :nohands :see-invis :oviparous :carnivore :poisonous :hostile :strong :nasty :picks-gold :picks-jewels :picks-magic-items})
  (MonsterType. "yellow dragon", \D, :yellow, 15, 9, -1, 20, 7, #{:genocidable}, [(MonsterAttack. :breath :acid 4 6) (MonsterAttack. :bite :physical 3 8) (MonsterAttack. :claw :physical 1 4) (MonsterAttack. :claw :physical 1 4)], 4500, 1500, :roar, :gigantic, #{:acid :stone}, #{}, #{:fly :thick-hide :nohands :see-invis :oviparous :carnivore :acid :hostile :strong :nasty :picks-gold :picks-jewels :picks-magic-items})
  (MonsterType. "stalker", \E, :white, 8, 12, 3, 0, 0, #{:genocidable}, [(MonsterAttack. :claw :physical 4 4)], 900, 400, :silent, :large, #{}, #{}, #{:animal :fly :see-invis :wander :follows :hostile :strong :infravision})
  (MonsterType. "air elemental", \E, :cyan, 8, 36, 2, 30, 0, #{:no-corpse}, [(MonsterAttack. :engulf :physical 1 10)], 0, 0, :silent, :huge, #{:poison :stone}, #{}, #{:noeyes :nolimbs :nohead :mindless :unsolid :fly :strong :neuter})
  (MonsterType. "fire elemental", \E, :yellow, 8, 12, 2, 30, 0, #{:no-corpse}, [(MonsterAttack. :claw :fire 3 6) (MonsterAttack. :passive :fire 0 4)], 0, 0, :silent, :huge, #{:fire :poison :stone}, #{}, #{:noeyes :nolimbs :nohead :mindless :unsolid :fly :notake :strong :neuter :infravisible})
  (MonsterType. "earth elemental", \E, :brown, 8, 6, 2, 30, 0, #{:no-corpse}, [(MonsterAttack. :claw :physical 4 6)], 2500, 0, :silent, :huge, #{:fire :cold :poison :stone}, #{}, #{:noeyes :nolimbs :nohead :mindless :breathless :phase :thick-hide :strong :neuter})
  (MonsterType. "water elemental", \E, :blue, 8, 6, 2, 30, 0, #{:no-corpse}, [(MonsterAttack. :claw :physical 5 6)], 2500, 0, :silent, :huge, #{:poison :stone}, #{}, #{:noeyes :nolimbs :nohead :mindless :amphibious :swim :strong :neuter})
  (MonsterType. "lichen", \F, :bright-green, 0, 1, 9, 0, 0, #{:genocidable}, [(MonsterAttack. :touch :stick 0 0)], 20, 200, :silent, :small, #{}, #{}, #{:breathless :noeyes :nolimbs :nohead :mindless :notake :hostile :neuter})
  (MonsterType. "brown mold", \F, :brown, 1, 0, 9, 0, 0, #{:genocidable}, [(MonsterAttack. :passive :cold 0 6)], 50, 30, :silent, :small, #{:cold :poison}, #{:cold :poison}, #{:breathless :noeyes :nolimbs :nohead :mindless :notake :hostile :neuter :sessile})
  (MonsterType. "yellow mold", \F, :yellow, 1, 0, 9, 0, 0, #{:genocidable}, [(MonsterAttack. :passive :stun 0 4)], 50, 30, :silent, :small, #{:poison}, #{:poison}, #{:breathless :noeyes :nolimbs :nohead :mindless :poisonous :notake :hostile :neuter :sessile})
  (MonsterType. "green mold", \F, :green, 1, 0, 9, 0, 0, #{:genocidable}, [(MonsterAttack. :passive :acid 0 4)], 50, 30, :silent, :small, #{:acid :stone}, #{}, #{:breathless :noeyes :nolimbs :nohead :mindless :acid :notake :hostile :neuter :sessile})
  (MonsterType. "red mold", \F, :red, 1, 0, 9, 0, 0, #{:genocidable}, [(MonsterAttack. :passive :fire 0 4)], 50, 30, :silent, :small, #{:fire :poison}, #{:fire :poison}, #{:breathless :noeyes :nolimbs :nohead :mindless :notake :hostile :neuter :infravisible :sessile})
  (MonsterType. "shrieker", \F, :magenta, 3, 1, 7, 0, 0, #{:genocidable}, [], 100, 100, :shriek, :small, #{:poison}, #{:poison}, #{:breathless :noeyes :nolimbs :nohead :mindless :notake :hostile :neuter})
  (MonsterType. "violet fungus", \F, :magenta, 3, 1, 7, 0, 0, #{:genocidable}, [(MonsterAttack. :touch :physical 1 4) (MonsterAttack. :touch :stick 0 0)], 100, 100, :silent, :small, #{:poison}, #{:poison}, #{:breathless :noeyes :nolimbs :nohead :mindless :notake :hostile :neuter})
  (MonsterType. "gnome", \G, :brown, 1, 6, 10, 4, 0, #{:genocidable :sgroup}, [(MonsterAttack. :weapon :physical 1 6)], 650, 100, :grunt, :small, #{}, #{}, #{:humanoid :omnivore :nopoly :gnome :collect :infravisible :infravision})
  (MonsterType. "gnome lord", \G, :blue, 3, 8, 10, 4, 0, #{:genocidable}, [(MonsterAttack. :weapon :physical 1 8)], 700, 120, :grunt, :small, #{}, #{}, #{:humanoid :omnivore :gnome :lord :male :collect :infravisible :infravision})
  (MonsterType. "gnomish wizard", \G, :bright-blue, 3, 10, 4, 10, 0, #{:genocidable}, [(MonsterAttack. :magic :spell 0 0)], 700, 120, :grunt, :small, #{}, #{}, #{:humanoid :omnivore :gnome :picks-magic-items :infravisible :infravision})
  (MonsterType. "gnome king", \G, :magenta, 5, 10, 10, 20, 0, #{:genocidable}, [(MonsterAttack. :weapon :physical 2 6)], 750, 150, :grunt, :small, #{}, #{}, #{:humanoid :omnivore :gnome :prince :male :collect :infravisible :infravision})
  (MonsterType. "giant", \H, :red, 6, 6, 0, 0, 2, #{:genocidable :not-generated}, [(MonsterAttack. :weapon :physical 2 10)], 2250, 750, :boast, :huge, #{}, #{}, #{:humanoid :carnivore :giant :strong :throws-boulders :nasty :collect :picks-jewels :infravisible :infravision})
  (MonsterType. "stone giant", \H, nil, 6, 6, 0, 0, 2, #{:genocidable :sgroup}, [(MonsterAttack. :weapon :physical 2 10)], 2250, 750, :boast, :huge, #{}, #{}, #{:humanoid :carnivore :giant :strong :throws-boulders :nasty :collect :picks-jewels :infravisible :infravision :str})
  (MonsterType. "hill giant", \H, :cyan, 8, 10, 6, 0, -2, #{:genocidable :sgroup}, [(MonsterAttack. :weapon :physical 2 8)], 2200, 700, :boast, :huge, #{}, #{}, #{:humanoid :carnivore :giant :strong :throws-boulders :nasty :collect :picks-jewels :infravisible :infravision :str})
  (MonsterType. "fire giant", \H, :yellow, 9, 12, 4, 5, 2, #{:genocidable :sgroup}, [(MonsterAttack. :weapon :physical 2 10)], 2250, 750, :boast, :huge, #{:fire}, #{:fire}, #{:humanoid :carnivore :giant :strong :throws-boulders :nasty :collect :picks-jewels :infravisible :infravision :str})
  (MonsterType. "frost giant", \H, :white, 10, 12, 3, 10, -3, #{:no-hell :genocidable :sgroup}, [(MonsterAttack. :weapon :physical 2 12)], 2250, 750, :boast, :huge, #{:cold}, #{:cold}, #{:humanoid :carnivore :giant :strong :throws-boulders :nasty :collect :picks-jewels :infravisible :infravision :str})
  (MonsterType. "storm giant", \H, :blue, 16, 12, 3, 10, -3, #{:genocidable :sgroup}, [(MonsterAttack. :weapon :physical 2 12)], 2250, 750, :boast, :huge, #{:shock}, #{:shock}, #{:humanoid :carnivore :giant :strong :throws-boulders :nasty :collect :picks-jewels :infravisible :infravision :str})
  (MonsterType. "ettin", \H, :brown, 10, 12, 3, 0, 0, #{:genocidable}, [(MonsterAttack. :weapon :physical 2 8) (MonsterAttack. :weapon :physical 3 6)], 1700, 500, :grunt, :huge, #{}, #{}, #{:animal :humanoid :carnivore :hostile :strong :nasty :collect :infravisible :infravision})
  (MonsterType. "titan", \H, :magenta, 16, 18, -3, 70, 9, #{}, [(MonsterAttack. :weapon :physical 2 8) (MonsterAttack. :magic :spell 0 0)], 2300, 900, :spell, :huge, #{}, #{}, #{:fly :humanoid :omnivore :strong :throws-boulders :nasty :collect :picks-magic-items :infravisible :infravision})
  (MonsterType. "minotaur", \H, :brown, 15, 15, 6, 0, 0, #{:genocidable :not-generated}, [(MonsterAttack. :claw :physical 3 10) (MonsterAttack. :claw :physical 3 10) (MonsterAttack. :butt :physical 2 8)], 1500, 700, :silent, :large, #{:elbereth}, #{}, #{:animal :humanoid :carnivore :hostile :strong :nasty :infravisible :infravision})
  (MonsterType. "jabberwock", \J, :orange, 15, 12, -2, 50, 0, #{:genocidable}, [(MonsterAttack. :bite :physical 2 10) (MonsterAttack. :bite :physical 2 10) (MonsterAttack. :claw :physical 2 10) (MonsterAttack. :claw :physical 2 10)], 1300, 600, :burble, :large, #{}, #{}, #{:animal :fly :carnivore :hostile :strong :nasty :collect :infravisible})
  (MonsterType. "lich", \L, :brown, 11, 6, 0, 30, -9, #{:genocidable :no-corpse}, [(MonsterAttack. :touch :cold 1 10) (MonsterAttack. :magic :spell 0 0)], 1200, 100, :mumble, :human, #{:cold :sleep :poison}, #{:cold}, #{:breathless :humanoid :poisonous :regen :undead :hostile :picks-magic-items :infravision})
  (MonsterType. "demilich", \L, :red, 14, 9, -2, 60, -12, #{:genocidable :no-corpse}, [(MonsterAttack. :touch :cold 3 4) (MonsterAttack. :magic :spell 0 0)], 1200, 100, :mumble, :human, #{:cold :sleep :poison}, #{:cold}, #{:breathless :humanoid :poisonous :regen :undead :hostile :picks-magic-items :infravision})
  (MonsterType. "master lich", \L, :magenta, 17, 9, -4, 90, -15, #{:hell-only :genocidable :no-corpse}, [(MonsterAttack. :touch :cold 3 6) (MonsterAttack. :magic :spell 0 0)], 1200, 100, :mumble, :human, #{:fire :cold :sleep :poison}, #{:fire :cold}, #{:breathless :humanoid :poisonous :regen :undead :hostile :picks-magic-items :wants-book :infravision})
  (MonsterType. "arch-lich", \L, :magenta, 25, 9, -6, 90, -15, #{:hell-only :genocidable :no-corpse}, [(MonsterAttack. :touch :cold 5 6) (MonsterAttack. :magic :spell 0 0)], 1200, 100, :mumble, :human, #{:fire :cold :sleep :shock :poison}, #{:fire :cold}, #{:breathless :humanoid :poisonous :regen :undead :hostile :picks-magic-items :wants-book :infravision})
  (MonsterType. "kobold mummy", \M, :brown, 3, 8, 6, 20, -2, #{:genocidable :no-corpse}, [(MonsterAttack. :claw :physical 1 4)], 400, 50, :silent, :small, #{:cold :sleep :poison}, #{}, #{:breathless :mindless :humanoid :poisonous :undead :hostile :infravision})
  (MonsterType. "gnome mummy", \M, :red, 4, 10, 6, 20, -3, #{:genocidable :no-corpse}, [(MonsterAttack. :claw :physical 1 6)], 650, 50, :silent, :small, #{:cold :sleep :poison}, #{}, #{:breathless :mindless :humanoid :poisonous :undead :hostile :gnome :infravision})
  (MonsterType. "orc mummy", \M, nil, 5, 10, 5, 20, -4, #{:genocidable :no-corpse}, [(MonsterAttack. :claw :physical 1 6)], 850, 75, :silent, :human, #{:cold :sleep :poison}, #{}, #{:breathless :mindless :humanoid :poisonous :undead :hostile :orc :picks-gold :picks-jewels :infravision})
  (MonsterType. "dwarf mummy", \M, :red, 5, 10, 5, 20, -4, #{:genocidable :no-corpse}, [(MonsterAttack. :claw :physical 1 6)], 900, 150, :silent, :human, #{:cold :sleep :poison}, #{}, #{:breathless :mindless :humanoid :poisonous :undead :hostile :dwarf :picks-gold :picks-jewels :infravision})
  (MonsterType. "elf mummy", \M, :green, 6, 12, 4, 30, -5, #{:genocidable :no-corpse}, [(MonsterAttack. :claw :physical 2 4)], 800, 175, :silent, :human, #{:cold :sleep :poison}, #{}, #{:breathless :mindless :humanoid :poisonous :undead :hostile :elf :infravision})
  (MonsterType. "human mummy", \M, nil, 6, 12, 4, 30, -5, #{:genocidable :no-corpse}, [(MonsterAttack. :claw :physical 2 4) (MonsterAttack. :claw :physical 2 4)], 1450, 200, :silent, :human, #{:cold :sleep :poison}, #{}, #{:breathless :mindless :humanoid :poisonous :undead :hostile :infravision})
  (MonsterType. "ettin mummy", \M, :blue, 7, 12, 4, 30, -6, #{:genocidable :no-corpse}, [(MonsterAttack. :claw :physical 2 6) (MonsterAttack. :claw :physical 2 6)], 1700, 250, :silent, :huge, #{:cold :sleep :poison}, #{}, #{:breathless :mindless :humanoid :poisonous :undead :hostile :strong :infravision})
  (MonsterType. "giant mummy", \M, :cyan, 8, 14, 3, 30, -7, #{:genocidable :no-corpse}, [(MonsterAttack. :claw :physical 3 4) (MonsterAttack. :claw :physical 3 4)], 2050, 375, :silent, :huge, #{:cold :sleep :poison}, #{}, #{:breathless :mindless :humanoid :poisonous :undead :hostile :giant :strong :picks-jewels :infravision})
  (MonsterType. "red naga hatchling", \N, :red, 3, 10, 6, 0, 0, #{:genocidable}, [(MonsterAttack. :bite :physical 1 4)], 500, 100, :mumble, :large, #{:fire :poison}, #{:fire :poison}, #{:nolimbs :slithy :thick-hide :notake :omnivore :strong :infravisible})
  (MonsterType. "black naga hatchling", \N, :blue, 3, 10, 6, 0, 0, #{:genocidable}, [(MonsterAttack. :bite :physical 1 4)], 500, 100, :mumble, :large, #{:poison :acid :stone}, #{:poison }, #{:nolimbs :slithy :thick-hide :acid :notake :carnivore :strong})
  (MonsterType. "golden naga hatchling", \N, :yellow, 3, 10, 6, 0, 0, #{:genocidable}, [(MonsterAttack. :bite :physical 1 4)], 500, 100, :mumble, :large, #{:poison}, #{:poison}, #{:nolimbs :slithy :thick-hide :notake :omnivore :strong})
  (MonsterType. "guardian naga hatchling", \N, :green, 3, 10, 6, 0, 0, #{:genocidable}, [(MonsterAttack. :bite :physical 1 4)], 500, 100, :mumble, :large, #{:poison}, #{:poison}, #{:nolimbs :slithy :thick-hide :notake :omnivore :strong})
  (MonsterType. "red naga", \N, :red, 6, 12, 4, 0, -4, #{:genocidable}, [(MonsterAttack. :bite :physical 2 4) (MonsterAttack. :breath :fire 2 6)], 2600, 400, :mumble, :huge, #{:fire :poison}, #{:fire :poison}, #{:nolimbs :slithy :thick-hide :oviparous :notake :omnivore :strong :infravisible})
  (MonsterType. "black naga", \N, :blue, 8, 14, 2, 10, 4, #{:genocidable}, [(MonsterAttack. :bite :physical 2 6) (MonsterAttack. :spit :acid 0 0)], 2600, 400, :mumble, :huge, #{:poison :acid :stone}, #{:poison }, #{:nolimbs :slithy :thick-hide :oviparous :acid :notake :carnivore :strong})
  (MonsterType. "golden naga", \N, :yellow, 10, 14, 2, 70, 5, #{:genocidable}, [(MonsterAttack. :bite :physical 2 6) (MonsterAttack. :magic :spell 4 6)], 2600, 400, :mumble, :huge, #{:poison}, #{:poison}, #{:nolimbs :slithy :thick-hide :oviparous :notake :omnivore :strong})
  (MonsterType. "guardian naga", \N, :green, 12, 16, 0, 50, 7, #{:genocidable}, [(MonsterAttack. :bite :paralysis 1 6) (MonsterAttack. :spit :poison 1 6) (MonsterAttack. :hug :physical 2 4)], 2600, 400, :mumble, :huge, #{:poison}, #{:poison}, #{:nolimbs :slithy :thick-hide :oviparous :poisonous :notake :omnivore :strong})
  (MonsterType. "ogre", \O, :brown, 5, 10, 5, 0, -3, #{:sgroup :genocidable}, [(MonsterAttack. :weapon :physical 2 5)], 1600, 500, :grunt, :large, #{}, #{}, #{:humanoid :carnivore :strong :picks-gold :picks-jewels :collect :infravisible :infravision})
  (MonsterType. "ogre lord", \O, :red, 7, 12, 3, 30, -5, #{:genocidable}, [(MonsterAttack. :weapon :physical 2 6)], 1700, 700, :grunt, :large, #{}, #{}, #{:humanoid :carnivore :strong :lord :male :picks-gold :picks-jewels :collect :infravisible :infravision})
  (MonsterType. "ogre king", \O, :magenta, 9, 14, 4, 60, -7, #{:genocidable}, [(MonsterAttack. :weapon :physical 3 5)], 1700, 750, :grunt, :large, #{}, #{}, #{:humanoid :carnivore :strong :prince :male :picks-gold :picks-jewels :collect :infravisible :infravision})
  (MonsterType. "gray ooze", \P, nil, 3, 1, 8, 0, 0, #{:genocidable}, [(MonsterAttack. :bite :rust 2 8)], 500, 250, :silent, :medium, #{:fire :cold :poison :acid :stone}, #{:fire :cold :poison}, #{:breathless :amorphous :noeyes :nolimbs :nohead :mindless :omnivore :acid :hostile :neuter})
  (MonsterType. "brown pudding", \P, :brown, 5, 3, 8, 0, 0, #{:genocidable}, [(MonsterAttack. :bite :rot 0 0)], 500, 250, :silent, :medium, #{:cold :shock :poison :acid :stone}, #{:cold :shock :poison}, #{:breathless :amorphous :noeyes :nolimbs :nohead :mindless :omnivore :acid :hostile :neuter})
  (MonsterType. "black pudding", \P, :blue, 10, 6, 6, 0, 0, #{:genocidable}, [(MonsterAttack. :bite :corrode 3 8) (MonsterAttack. :passive :corrode 0 0)], 900, 250, :silent, :large, #{:cold :shock :poison :acid :stone}, #{:cold :shock :poison}, #{:breathless :amorphous :noeyes :nolimbs :nohead :mindless :omnivore :acid :hostile :neuter})
  (MonsterType. "green slime", \P, :green, 6, 6, 6, 0, 0, #{:hell-only :genocidable}, [(MonsterAttack. :touch :slime 1 4) (MonsterAttack. :passive :slime 0 0)], 400, 150, :silent, :large, #{:cold :shock :poison :acid :stone}, #{}, #{:breathless :amorphous :noeyes :nolimbs :nohead :mindless :omnivore :acid :poisonous :hostile :neuter})
  (MonsterType. "quantum mechanic", \Q, :cyan, 7, 12, 3, 10, 0, #{:genocidable}, [(MonsterAttack. :claw :teleport 1 4)], 1450, 20, :humanoid, :human, #{:poison}, #{}, #{:humanoid :omnivore :poisonous :teleport :hostile :infravisible})
  (MonsterType. "rust monster", \R, :brown, 5, 18, 2, 0, 0, #{:genocidable}, [(MonsterAttack. :touch :rust 0 0) (MonsterAttack. :touch :rust 0 0) (MonsterAttack. :passive :rust 0 0)], 1000, 250, :silent, :medium, #{}, #{}, #{:swim :animal :nohands :metallivore :hostile :infravisible})
  (MonsterType. "disenchanter", \R, :blue, 12, 12, -10, 0, -3, #{:hell-only :genocidable}, [(MonsterAttack. :claw :disenchant 4 4) (MonsterAttack. :passive :disenchant 0 0)], 750, 200, :growl, :large, #{}, #{}, #{:animal :carnivore :hostile :infravisible})
  (MonsterType. "garter snake", \S, :green, 1, 8, 8, 0, 0, #{:lgroup :genocidable}, [(MonsterAttack. :bite :physical 1 2)], 50, 60, :hiss, :tiny, #{}, #{}, #{:swim :conceal :nolimbs :animal :slithy :oviparous :carnivore :notake})
  (MonsterType. "snake", \S, :brown, 4, 15, 3, 0, 0, #{:genocidable}, [(MonsterAttack. :bite :poison 1 6)], 100, 80, :hiss, :small, #{:poison}, #{:poison}, #{:swim :conceal :nolimbs :animal :slithy :poisonous :oviparous :carnivore :notake :hostile})
  (MonsterType. "water moccasin", \S, :red, 4, 15, 3, 0, 0, #{:genocidable :not-generated :lgroup}, [(MonsterAttack. :bite :poison 1 6)], 150, 80, :hiss, :small, #{:poison}, #{:poison}, #{:swim :conceal :nolimbs :animal :slithy :poisonous :carnivore :oviparous :notake :hostile})
  (MonsterType. "pit viper", \S, :blue, 6, 15, 2, 0, 0, #{:genocidable}, [(MonsterAttack. :bite :poison 1 4) (MonsterAttack. :bite :poison 1 4)], 100, 60, :hiss, :medium, #{:poison}, #{:poison}, #{:swim :conceal :nolimbs :animal :slithy :poisonous :carnivore :oviparous :notake :hostile :infravision})
  (MonsterType. "python", \S, :magenta, 6, 3, 5, 0, 0, #{:genocidable}, [(MonsterAttack. :bite :physical 1 4) (MonsterAttack. :touch :physical 0 0) (MonsterAttack. :hug :wrap 1 4) (MonsterAttack. :hug :physical 2 4)], 250, 100, :hiss, :large, #{}, #{}, #{:swim :nolimbs :animal :slithy :carnivore :oviparous :notake :hostile :strong :infravision})
  (MonsterType. "cobra", \S, :blue, 6, 18, 2, 0, 0, #{:genocidable}, [(MonsterAttack. :bite :poison 2 4) (MonsterAttack. :spit :blinding 0 0)], 250, 100, :hiss, :medium, #{:poison}, #{:poison}, #{:swim :conceal :nolimbs :animal :slithy :poisonous :carnivore :oviparous :notake :hostile})
  (MonsterType. "troll", \T, :brown, 7, 12, 4, 0, -3, #{:genocidable}, [(MonsterAttack. :weapon :physical 4 2) (MonsterAttack. :claw :physical 4 2) (MonsterAttack. :bite :physical 2 6)], 800, 350, :grunt, :large, #{}, #{}, #{:humanoid :regen :carnivore :strong :follows :hostile :infravisible :infravision})
  (MonsterType. "ice troll", \T, :white, 9, 10, 2, 20, -3, #{:no-hell :genocidable}, [(MonsterAttack. :weapon :physical 2 6) (MonsterAttack. :claw :cold 2 6) (MonsterAttack. :bite :physical 2 6)], 1000, 300, :grunt, :large, #{:cold}, #{:cold}, #{:humanoid :regen :carnivore :strong :follows :hostile :infravisible :infravision})
  (MonsterType. "rock troll", \T, :cyan, 9, 12, 0, 0, -3, #{:genocidable}, [(MonsterAttack. :weapon :physical 3 6) (MonsterAttack. :claw :physical 2 8) (MonsterAttack. :bite :physical 2 6)], 1200, 300, :grunt, :large, #{}, #{}, #{:humanoid :regen :carnivore :strong :follows :hostile :collect :infravisible :infravision})
  (MonsterType. "water troll", \T, :blue, 11, 14, 4, 40, -3, #{:not-generated :genocidable}, [(MonsterAttack. :weapon :physical 2 8) (MonsterAttack. :claw :physical 2 8) (MonsterAttack. :bite :physical 2 6)], 1200, 350, :grunt, :large, #{}, #{}, #{:humanoid :regen :carnivore :swim :strong :follows :hostile :infravisible :infravision})
  (MonsterType. "Olog-hai", \T, :magenta, 13, 12, -4, 0, -7, #{:genocidable}, [(MonsterAttack. :weapon :physical 3 6) (MonsterAttack. :claw :physical 2 8) (MonsterAttack. :bite :physical 2 6)], 1500, 400, :grunt, :large, #{}, #{}, #{:humanoid :regen :carnivore :strong :follows :hostile :collect :infravisible :infravision})
  (MonsterType. "umber hulk", \U, :brown, 9, 6, 2, 25, 0, #{:genocidable}, [(MonsterAttack. :claw :physical 3 4) (MonsterAttack. :claw :physical 3 4) (MonsterAttack. :bite :physical 2 5) (MonsterAttack. :gaze :conf 0 0)], 1200, 500, :silent, :large, #{}, #{}, #{:tunnel :carnivore :strong :infravisible})
  (MonsterType. "vampire", \V, :red, 10, 12, 1, 25, -8, #{:genocidable :no-corpse}, [(MonsterAttack. :claw :physical 1 6) (MonsterAttack. :bite :drain-xp 1 6)], 1450, 400, :vampire, :human, #{:sleep :poison}, #{}, #{:fly :breathless :humanoid :poisonous :regen :undead :follows :hostile :strong :nasty :infravisible})
  (MonsterType. "vampire lord", \V, :blue, 12, 14, 0, 50, -9, #{:genocidable :no-corpse}, [(MonsterAttack. :claw :physical 1 8) (MonsterAttack. :bite :drain-xp 1 8)], 1450, 400, :vampire, :human, #{:sleep :poison}, #{}, #{:fly :breathless :humanoid :poisonous :regen :undead :follows :hostile :strong :nasty :lord :male :infravisible})
  (MonsterType. "Vlad the Impaler", \V, :magenta, 14, 18, -3, 80, -10, #{:not-generated :no-corpse :unique}, [(MonsterAttack. :weapon :physical 1 10) (MonsterAttack. :bite :drain-xp 1 10)], 1450, 400, :vampire, :human, #{:sleep :poison}, #{}, #{:fly :breathless :humanoid :poisonous :regen :nopoly :undead :follows :hostile :proper-name :strong :nasty :prince :male :waits :wants-candelabrum :infravisible})
  (MonsterType. "barrow wight", \W, nil, 3, 12, 5, 5, -3, #{:genocidable :no-corpse}, [(MonsterAttack. :weapon :drain-xp 0 0) (MonsterAttack. :magic :spell 0 0) (MonsterAttack. :claw :physical 1 4)], 1200, 0, :spell, :human, #{:cold :sleep :poison}, #{}, #{:breathless :humanoid :undead :follows :hostile :collect})
  (MonsterType. "wraith", \W, :blue, 6, 12, 4, 15, -6, #{:genocidable}, [(MonsterAttack. :touch :drain-xp 1 6)], 0, 0, :silent, :human, #{:cold :sleep :poison :stone}, #{}, #{:breathless :fly :humanoid :unsolid :undead :follows :hostile})
  (MonsterType. "Nazgul", \W, :magenta, 13, 12, 0, 25, -17, #{:genocidable :no-corpse}, [(MonsterAttack. :weapon :drain-xp 1 4) (MonsterAttack. :breath :sleep 2 25)], 1450, 0, :spell, :human, #{:cold :sleep :poison}, #{}, #{:breathless :humanoid :nopoly :undead :follows :strong :hostile :male :collect})
  (MonsterType. "xorn", \X, :brown, 8, 9, -2, 20, 0, #{:genocidable}, [(MonsterAttack. :claw :physical 1 3) (MonsterAttack. :claw :physical 1 3) (MonsterAttack. :claw :physical 1 3) (MonsterAttack. :bite :physical 4 6)], 1200, 700, :roar, :medium, #{:fire :cold :stone}, #{}, #{:breathless :phase :thick-hide :metallivore :hostile :strong})
  (MonsterType. "monkey", \Y, nil, 2, 12, 6, 0, 0, #{:genocidable}, [(MonsterAttack. :claw :steal-items 0 0) (MonsterAttack. :bite :physical 1 3)], 100, 50, :growl, :small, #{}, #{}, #{:animal :humanoid :carnivore 0 :infravisible})
  (MonsterType. "ape", \Y, :brown, 4, 12, 6, 0, 0, #{:genocidable :sgroup}, [(MonsterAttack. :claw :physical 1 3) (MonsterAttack. :claw :physical 1 3) (MonsterAttack. :bite :physical 1 6)], 1100, 500, :growl, :large, #{}, #{}, #{:animal :humanoid :carnivore :strong :infravisible})
  (MonsterType. "owlbear", \Y, :brown, 5, 12, 5, 0, 0, #{:genocidable}, [(MonsterAttack. :claw :physical 1 6) (MonsterAttack. :claw :physical 1 6) (MonsterAttack. :hug :physical 2 8)], 1700, 700, :roar, :large, #{}, #{}, #{:animal :humanoid :carnivore :hostile :strong :nasty :infravisible})
  (MonsterType. "yeti", \Y, :white, 5, 15, 6, 0, 0, #{:genocidable}, [(MonsterAttack. :claw :physical 1 6) (MonsterAttack. :claw :physical 1 6) (MonsterAttack. :bite :physical 1 4)], 1600, 700, :growl, :large, #{:cold}, #{:cold}, #{:animal :humanoid :carnivore :hostile :strong :infravisible})
  (MonsterType. "carnivorous ape", \Y, :blue, 6, 12, 6, 0, 0, #{:genocidable}, [(MonsterAttack. :claw :physical 1 4) (MonsterAttack. :claw :physical 1 4) (MonsterAttack. :hug :physical 1 8)], 1250, 550, :growl, :large, #{}, #{}, #{:animal :humanoid :carnivore :hostile :strong :infravisible})
  (MonsterType. "sasquatch", \Y, nil, 7, 15, 6, 0, 2, #{:genocidable}, [(MonsterAttack. :claw :physical 1 6) (MonsterAttack. :claw :physical 1 6) (MonsterAttack. :kick :physical 1 8)], 1550, 750, :growl, :large, #{}, #{}, #{:animal :humanoid :see-invis :omnivore :strong :infravisible})
  (MonsterType. "kobold zombie", \Z, :brown, 0, 6, 10, 0, -2, #{:genocidable :no-corpse}, [(MonsterAttack. :claw :physical 1 4)], 400, 50, :silent, :small, #{:cold :sleep :poison}, #{}, #{:breathless :mindless :humanoid :poisonous :undead :follows :hostile :infravision})
  (MonsterType. "gnome zombie", \Z, :brown, 1, 6, 10, 0, -2, #{:genocidable :no-corpse}, [(MonsterAttack. :claw :physical 1 5)], 650, 50, :silent, :small, #{:cold :sleep :poison}, #{}, #{:breathless :mindless :humanoid :poisonous :undead :follows :hostile :gnome :infravision})
  (MonsterType. "orc zombie", \Z, nil, 2, 6, 9, 0, -3, #{:genocidable :sgroup :no-corpse}, [(MonsterAttack. :claw :physical 1 6)], 850, 75, :silent, :human, #{:cold :sleep :poison}, #{}, #{:breathless :mindless :humanoid :poisonous :undead :follows :hostile :orc :infravision})
  (MonsterType. "dwarf zombie", \Z, :red, 2, 6, 9, 0, -3, #{:genocidable :sgroup :no-corpse}, [(MonsterAttack. :claw :physical 1 6)], 900, 150, :silent, :human, #{:cold :sleep :poison}, #{}, #{:breathless :mindless :humanoid :poisonous :undead :follows :hostile :dwarf :infravision})
  (MonsterType. "elf zombie", \Z, :green, 3, 6, 9, 0, -3, #{:genocidable :sgroup :no-corpse}, [(MonsterAttack. :claw :physical 1 7)], 800, 175, :silent, :human, #{:cold :sleep :poison}, #{}, #{:breathless :mindless :humanoid :undead :follows :hostile :elf :infravision})
  (MonsterType. "human zombie", \Z, :white, 4, 6, 8, 0, -3, #{:genocidable :sgroup :no-corpse}, [(MonsterAttack. :claw :physical 1 8)], 1450, 200, :silent, :human, #{:cold :sleep :poison}, #{}, #{:breathless :mindless :humanoid :undead :follows :hostile :infravision})
  (MonsterType. "ettin zombie", \Z, :blue, 6, 8, 6, 0, -4, #{:genocidable :no-corpse}, [(MonsterAttack. :claw :physical 1 10) (MonsterAttack. :claw :physical 1 10)], 1700, 250, :silent, :huge, #{:cold :sleep :poison}, #{}, #{:breathless :mindless :humanoid :undead :follows :hostile :strong :infravision})
  (MonsterType. "giant zombie", \Z, :cyan, 8, 8, 6, 0, -4, #{:genocidable :no-corpse}, [(MonsterAttack. :claw :physical 2 8) (MonsterAttack. :claw :physical 2 8)], 2050, 375, :silent, :huge, #{:cold :sleep :poison}, #{}, #{:breathless :mindless :humanoid :undead :follows :hostile :giant :strong :infravision})
  (MonsterType. "ghoul", \Z, :blue, 3, 6, 10, 0, -2, #{:genocidable :no-corpse}, [(MonsterAttack. :claw :paralysis 1 2) (MonsterAttack. :claw :physical 1 3)], 400, 50, :silent, :small, #{:cold :sleep :poison}, #{}, #{:breathless :mindless :humanoid :poisonous :undead :wander :hostile :infravision})
  (MonsterType. "skeleton", \Z, :white, 12, 8, 4, 0, 0, #{:no-corpse :not-generated}, [(MonsterAttack. :weapon :physical 2 6) (MonsterAttack. :touch :slow 1 6)], 300, 5, :bones, :human, #{:cold :sleep :poison :stone}, #{}, #{:breathless :mindless :humanoid :thick-hide :undead :wander :hostile :strong :collect :nasty :infravision})
  (MonsterType. "straw golem", \', :yellow, 3, 12, 10, 0, 0, #{:no-corpse}, [(MonsterAttack. :claw :physical 1 2) (MonsterAttack. :claw :physical 1 2)], 400, 0, :silent, :large, #{:sleep :poison}, #{}, #{:breathless :mindless :humanoid :hostile :neuter})
  (MonsterType. "paper golem", \', :white, 3, 12, 10, 0, 0, #{:no-corpse}, [(MonsterAttack. :claw :physical 1 3)], 400, 0, :silent, :large, #{:sleep :poison}, #{}, #{:breathless :mindless :humanoid :hostile :neuter})
  (MonsterType. "rope golem", \', :brown, 4, 9, 8, 0, 0, #{:no-corpse}, [(MonsterAttack. :claw :physical 1 4) (MonsterAttack. :claw :physical 1 4) (MonsterAttack. :hug :physical 6 1)], 450, 0, :silent, :large, #{:sleep :poison}, #{}, #{:breathless :mindless :humanoid :hostile :neuter})
  (MonsterType. "gold golem", \', :yellow, 5, 9, 6, 0, 0, #{:no-corpse}, [(MonsterAttack. :claw :physical 2 3) (MonsterAttack. :claw :physical 2 3)], 450, 0, :silent, :large, #{:sleep :poison :acid}, #{}, #{:breathless :mindless :humanoid :thick-hide :hostile :neuter})
  (MonsterType. "leather golem", \', :brown, 6, 6, 6, 0, 0, #{:no-corpse}, [(MonsterAttack. :claw :physical 1 6) (MonsterAttack. :claw :physical 1 6)], 800, 0, :silent, :large, #{:sleep :poison}, #{}, #{:breathless :mindless :humanoid :hostile :neuter})
  (MonsterType. "wood golem", \', :brown, 7, 3, 4, 0, 0, #{:no-corpse}, [(MonsterAttack. :claw :physical 3 4)], 900, 0, :silent, :large, #{:sleep :poison}, #{}, #{:breathless :mindless :humanoid :thick-hide :hostile :neuter})
  (MonsterType. "flesh golem", \', :red, 9, 8, 9, 30, 0, #{}, [(MonsterAttack. :claw :physical 2 8) (MonsterAttack. :claw :physical 2 8)], 1400, 600, :silent, :large, #{:fire :cold :shock :sleep :poison}, #{:fire :cold :shock :sleep :poison}, #{:breathless :mindless :humanoid :hostile :strong})
  (MonsterType. "clay golem", \', :brown, 11, 7, 7, 40, 0, #{:no-corpse}, [(MonsterAttack. :claw :physical 3 10)], 1550, 0, :silent, :large, #{:sleep :poison}, #{}, #{:breathless :mindless :humanoid :thick-hide :hostile :strong})
  (MonsterType. "stone golem", \', nil, 14, 6, 5, 50, 0, #{:no-corpse}, [(MonsterAttack. :claw :physical 3 8)], 1900, 0, :silent, :large, #{:sleep :poison :stone}, #{}, #{:breathless :mindless :humanoid :thick-hide :hostile :strong})
  (MonsterType. "glass golem", \', :cyan, 16, 6, 1, 50, 0, #{:no-corpse}, [(MonsterAttack. :claw :physical 2 8) (MonsterAttack. :claw :physical 2 8)], 1800, 0, :silent, :large, #{:sleep :poison :acid}, #{}, #{:breathless :mindless :humanoid :thick-hide :hostile :strong})
  (MonsterType. "iron golem", \', :cyan, 18, 6, 3, 60, 0, #{:no-corpse}, [(MonsterAttack. :weapon :physical 4 10) (MonsterAttack. :breath :poison 4 6)], 2000, 0, :silent, :large, #{:fire :cold :shock :sleep :poison}, #{}, #{:breathless :mindless :humanoid :thick-hide :poisonous :hostile :strong :collect})
  (MonsterType. "human", \@, :white, 0, 12, 10, 0, 0, #{:not-generated}, [(MonsterAttack. :weapon :physical 1 6)], 1450, 400, :humanoid, :human, #{:elbereth}, #{}, #{:humanoid :omnivore :nopoly :human :strong :collect :infravisible})
  (MonsterType. "wererat", \@, :brown, 2, 12, 10, 10, -7, #{}, [(MonsterAttack. :weapon :physical 2 4)], 1450, 400, :were, :human, #{:elbereth :poison}, #{}, #{:humanoid :poisonous :regen :omnivore :nopoly :were :hostile :human :collect :infravisible})
  (MonsterType. "werejackal", \@, :red, 2, 12, 10, 10, -7, #{}, [(MonsterAttack. :weapon :physical 2 4)], 1450, 400, :were, :human, #{:elbereth :poison}, #{}, #{:humanoid :poisonous :regen :omnivore :nopoly :were :hostile :human :collect :infravisible})
  (MonsterType. "werewolf", \@, :orange, 5, 12, 10, 20, -7, #{}, [(MonsterAttack. :weapon :physical 2 4)], 1450, 400, :were, :human, #{:elbereth :poison}, #{}, #{:humanoid :poisonous :regen :omnivore :nopoly :were :hostile :human :collect :infravisible})
  (MonsterType. "elf", \@, :white, 10, 12, 10, 2, -3, #{:not-generated}, [(MonsterAttack. :weapon :physical 1 8)], 800, 350, :humanoid, :human, #{:elbereth :sleep}, #{:sleep}, #{:humanoid :omnivore :see-invis :nopoly :elf :strong :collect :infravision :infravisible})
  (MonsterType. "Woodland-elf", \@, :green, 4, 12, 10, 10, -5, #{:genocidable :sgroup}, [(MonsterAttack. :weapon :physical 2 4)], 800, 350, :humanoid, :human, #{:elbereth :sleep}, #{:sleep}, #{:humanoid :omnivore :see-invis :elf :collect :infravisible :infravision})
  (MonsterType. "Green-elf", \@, :bright-green, 5, 12, 10, 10, -6, #{:genocidable :sgroup}, [(MonsterAttack. :weapon :physical 2 4)], 800, 350, :humanoid, :human, #{:elbereth :sleep}, #{:sleep}, #{:humanoid :omnivore :see-invis :elf :collect :infravisible :infravision})
  (MonsterType. "Grey-elf", \@, nil, 6, 12, 10, 10, -7, #{:genocidable :sgroup}, [(MonsterAttack. :weapon :physical 2 4)], 800, 350, :humanoid, :human, #{:elbereth :sleep}, #{:sleep}, #{:humanoid :omnivore :see-invis :elf :collect :infravisible :infravision})
  (MonsterType. "elf-lord", \@, :bright-blue, 8, 12, 10, 20, -9, #{:genocidable :sgroup}, [(MonsterAttack. :weapon :physical 2 4) (MonsterAttack. :weapon :physical 2 4)], 800, 350, :humanoid, :human, #{:elbereth :sleep}, #{:sleep}, #{:humanoid :omnivore :see-invis :elf :strong :lord :male :collect :infravisible :infravision})
  (MonsterType. "Elvenking", \@, :magenta, 9, 12, 10, 25, -10, #{:genocidable}, [(MonsterAttack. :weapon :physical 2 4) (MonsterAttack. :weapon :physical 2 4)], 800, 350, :humanoid, :human, #{:elbereth :sleep}, #{:sleep}, #{:humanoid :omnivore :see-invis :elf :strong :prince :male :collect :infravisible :infravision})
  (MonsterType. "doppelganger", \@, :white, 9, 12, 5, 20, 0, #{:genocidable}, [(MonsterAttack. :weapon :physical 1 12)], 1450, 400, :imitate, :human, #{:elbereth :sleep}, #{}, #{:humanoid :omnivore :nopoly :human :hostile :strong :collect :infravisible})
  (MonsterType. "nurse", \@, :white, 11, 6, 0, 0, 0, #{:genocidable}, [(MonsterAttack. :claw :heal 2 6)], 1450, 400, :nurse, :human, #{:elbereth :poison}, #{:poison}, #{:humanoid :omnivore :nopoly :human :hostile :infravisible})
  (MonsterType. "shopkeeper", \@, :white, 12, 18, 0, 50, 0, #{:not-generated}, [(MonsterAttack. :weapon :physical 4 4) (MonsterAttack. :weapon :physical 4 4)], 1450, 400, :sell, :human, #{:elbereth}, #{}, #{:humanoid :omnivore :nopoly :human :peaceful :strong :collect :picks-magic-items :infravisible})
  (MonsterType. "guard", \@, :blue, 12, 12, 10, 40, 10, #{:not-generated}, [(MonsterAttack. :weapon :physical 4 10)], 1450, 400, :guard, :human, #{:elbereth}, #{}, #{:humanoid :omnivore :nopoly :human :mercenary :peaceful :strong :collect :infravisible})
  (MonsterType. "prisoner", \@, :white, 12, 12, 10, 0, 0, #{:not-generated}, [(MonsterAttack. :weapon :physical 1 6)], 1450, 400, :djinni, :human, #{:elbereth}, #{}, #{:humanoid :omnivore :nopoly :human :peaceful :strong :collect :infravisible :approach})
  (MonsterType. "Oracle", \@, :bright-blue, 12, 0, 0, 50, 0, #{:not-generated :unique}, [(MonsterAttack. :passive :magic-missile 0 4)], 1450, 400, :oracle, :human, #{:elbereth}, #{}, #{:humanoid :omnivore :nopoly :human :peaceful :female :infravisible :sessile})
  (MonsterType. "aligned priest", \@, :white, 12, 12, 10, 50, 0, #{:not-generated}, [(MonsterAttack. :weapon :physical 4 10) (MonsterAttack. :kick :physical 1 4) (MonsterAttack. :magic :clerical 0 0)], 1450, 400, :priest, :human, #{:elbereth :shock}, #{}, #{:humanoid :omnivore :nopoly :human :lord :peaceful :collect :infravisible})
  (MonsterType. "high priest", \@, :white, 25, 15, 7, 70, 0, #{:not-generated :unique}, [(MonsterAttack. :weapon :physical 4 10) (MonsterAttack. :kick :physical 2 8) (MonsterAttack. :magic :clerical 2 8) (MonsterAttack. :magic :clerical 2 8)], 1450, 400, :priest, :human, #{:elbereth :fire :shock :sleep :poison}, #{}, #{:humanoid :see-invis :omnivore :proper-name :nopoly :human :minion :prince :nasty :collect :picks-magic-items :infravisible})
  (MonsterType. "soldier", \@, nil, 6, 10, 10, 0, -2, #{:sgroup :genocidable}, [(MonsterAttack. :weapon :physical 1 8)], 1450, 400, :soldier, :human, #{:elbereth}, #{}, #{:humanoid :omnivore :nopoly :human :mercenary :follows :hostile :strong :collect :infravisible})
  (MonsterType. "sergeant", \@, :red, 8, 10, 10, 5, -3, #{:sgroup :genocidable}, [(MonsterAttack. :weapon :physical 2 6)], 1450, 400, :soldier, :human, #{:elbereth}, #{}, #{:humanoid :omnivore :nopoly :human :mercenary :follows :hostile :strong :collect :infravisible})
  (MonsterType. "lieutenant", \@, :green, 10, 10, 10, 15, -4, #{:genocidable}, [(MonsterAttack. :weapon :physical 3 4) (MonsterAttack. :weapon :physical 3 4)], 1450, 400, :soldier, :human, #{:elbereth}, #{}, #{:humanoid :omnivore :nopoly :human :mercenary :follows :hostile :strong :collect :infravisible})
  (MonsterType. "captain", \@, :blue, 12, 10, 10, 15, -5, #{:genocidable}, [(MonsterAttack. :weapon :physical 4 4) (MonsterAttack. :weapon :physical 4 4)], 1450, 400, :soldier, :human, #{:elbereth}, #{}, #{:humanoid :omnivore :nopoly :human :mercenary :follows :hostile :strong :collect :infravisible})
  (MonsterType. "watchman", \@, nil, 6, 10, 10, 0, -2, #{:sgroup :not-generated :genocidable}, [(MonsterAttack. :weapon :physical 1 8)], 1450, 400, :soldier, :human, #{:elbereth}, #{}, #{:humanoid :omnivore :nopoly :human :mercenary :follows :peaceful :strong :collect :infravisible :guard})
  (MonsterType. "watch captain", \@, :green, 10, 10, 10, 15, -4, #{:not-generated :genocidable}, [(MonsterAttack. :weapon :physical 3 4) (MonsterAttack. :weapon :physical 3 4)], 1450, 400, :soldier, :human, #{:elbereth}, #{}, #{:humanoid :omnivore :nopoly :human :mercenary :follows :peaceful :strong :collect :infravisible :guard})
  (MonsterType. "Medusa", \@, :bright-green, 20, 12, 2, 50, -15, #{:not-generated :unique}, [(MonsterAttack. :weapon :physical 2 4) (MonsterAttack. :claw :physical 1 8) (MonsterAttack. :gaze :stone 0 0) (MonsterAttack. :bite :poison 1 6)], 1450, 400, :hiss, :large, #{:elbereth :poison :stone}, #{:poison}, #{:fly :swim :amphibious :humanoid :poisonous :omnivore :nopoly :hostile :strong :proper-name :female :waits :infravisible})
  (MonsterType. "Wizard of Yendor", \@, :magenta, 30, 12, -8, 100, -128, #{:not-generated :unique}, [(MonsterAttack. :claw :steal-amulet 2 12) (MonsterAttack. :magic :spell 0 0)], 1450, 400, :cuss, :human, #{:elbereth :fire :poison}, #{:fire :poison}, #{:proper-name :fly :breathless :humanoid :regen :see-invis :teleport :telecontrol :omnivore :nopoly :human :hostile :strong :nasty :prince :male :picks-magic-items :covetous :waits :infravisible})
  (MonsterType. "Croesus", \@, :magenta, 20, 15, 0, 40, 15, #{:unique :not-generated}, [(MonsterAttack. :weapon :physical 4 10)], 1450, 400, :guard, :human, #{:elbereth}, #{}, #{:humanoid :see-invis :omnivore :nopoly :human :follows :hostile :strong :nasty :proper-name :prince :male :picks-gold :picks-jewels :collect :picks-magic-items :infravisible})
  (MonsterType. "ghost", \X, nil, 10, 3, -5, 50, -5, #{:no-corpse :not-generated}, [(MonsterAttack. :touch :physical 1 1)], 1450, 0, :silent, :human, #{:cold :disintegration :sleep :poison :stone}, #{}, #{:fly :breathless :phase :humanoid :unsolid :nopoly :undead :follows :hostile :infravision})
  (MonsterType. "shade", \X, :blue, 12, 10, 10, 0, 0, #{:no-corpse :not-generated}, [(MonsterAttack. :touch :paralysis 2 6) (MonsterAttack. :touch :slow 1 6)], 1450, 0, :wail, :human, #{:cold :disintegration :sleep :poison :stone}, #{}, #{:fly :breathless :phase :humanoid :unsolid :see-invis :nopoly :undead :wander :follows :hostile :nasty :infravision})
  (MonsterType. "water demon", \&, :blue, 8, 12, -4, 30, -7, #{:no-corpse :not-generated}, [(MonsterAttack. :weapon :physical 1 3) (MonsterAttack. :claw :physical 1 3) (MonsterAttack. :bite :physical 1 3)], 1450, 400, :djinni, :human, #{:fire :poison}, #{}, #{:humanoid :poisonous :swim :nopoly :demon :follows :hostile :nasty :collect :infravisible :infravision})
  (MonsterType. "horned devil", \&, :brown, 6, 9, -5, 50, 11, #{:hell-only :no-corpse}, [(MonsterAttack. :weapon :physical 1 4) (MonsterAttack. :claw :physical 1 4) (MonsterAttack. :bite :physical 2 3) (MonsterAttack. :sting :physical 1 3)], 1450, 400, :silent, :human, #{:fire :poison}, #{}, #{:poisonous :thick-hide :demon :follows :hostile :nasty :infravisible :infravision})
  (MonsterType. "succubus", \&, nil, 6, 12, 0, 70, -9, #{:no-corpse}, [(MonsterAttack. :claw :physical 1 3) (MonsterAttack. :claw :physical 1 3) (MonsterAttack. :bite :drain-xp 2 6)], 1450, 400, :seduce, :human, #{:fire :poison}, #{}, #{:humanoid :fly :poisonous :demon :follows :hostile :nasty :female :infravisible :infravision})
  (MonsterType. "incubus", \&, nil, 6, 12, 0, 70, -9, #{:no-corpse}, [(MonsterAttack. :claw :physical 1 3) (MonsterAttack. :claw :physical 1 3) (MonsterAttack. :bite :drain-xp 2 6)], 1450, 400, :seduce, :human, #{:fire :poison}, #{}, #{:humanoid :fly :poisonous :demon :follows :hostile :nasty :male :infravisible :infravision})
  (MonsterType. "erinys", \&, :red, 7, 12, 2, 30, 10, #{:hell-only :no-corpse :sgroup}, [(MonsterAttack. :weapon :poison 2 4)], 1450, 400, :silent, :human, #{:fire :poison}, #{}, #{:humanoid :poisonous :nopoly :demon :follows :hostile :strong :nasty :female :collect :infravisible :infravision})
  (MonsterType. "barbed devil", \&, :red, 8, 12, 0, 35, 8, #{:hell-only :no-corpse :sgroup}, [(MonsterAttack. :claw :physical 2 4) (MonsterAttack. :claw :physical 2 4) (MonsterAttack. :sting :physical 3 4)], 1450, 400, :silent, :human, #{:fire :poison}, #{}, #{:poisonous :thick-hide :demon :follows :hostile :nasty :infravisible :infravision})
  (MonsterType. "marilith", \&, :red, 7, 12, -6, 80, -12, #{:hell-only :no-corpse}, [(MonsterAttack. :weapon :physical 2 4) (MonsterAttack. :weapon :physical 2 4) (MonsterAttack. :claw :physical 2 4) (MonsterAttack. :claw :physical 2 4) (MonsterAttack. :claw :physical 2 4) (MonsterAttack. :claw :physical 2 4)], 1450, 400, :cuss, :large, #{:fire :poison}, #{}, #{:humanoid :slithy :see-invis :poisonous :demon :follows :hostile :nasty :female :collect :infravisible :infravision})
  (MonsterType. "vrock", \&, :red, 8, 12, 0, 50, -9, #{:hell-only :no-corpse :sgroup}, [(MonsterAttack. :claw :physical 1 4) (MonsterAttack. :claw :physical 1 4) (MonsterAttack. :claw :physical 1 8) (MonsterAttack. :claw :physical 1 8) (MonsterAttack. :bite :physical 1 6)], 1450, 400, :silent, :large, #{:fire :poison}, #{}, #{:poisonous :demon :follows :hostile :nasty :infravisible :infravision})
  (MonsterType. "hezrou", \&, :red, 9, 6, -2, 55, -10, #{:hell-only :no-corpse :sgroup}, [(MonsterAttack. :claw :physical 1 3) (MonsterAttack. :claw :physical 1 3) (MonsterAttack. :bite :physical 4 4)], 1450, 400, :silent, :large, #{:fire :poison}, #{}, #{:humanoid :poisonous :demon :follows :hostile :nasty :infravisible :infravision})
  (MonsterType. "bone devil", \&, nil, 9, 15, -1, 40, -9, #{:hell-only :no-corpse :sgroup}, [(MonsterAttack. :weapon :physical 3 4) (MonsterAttack. :sting :poison 2 4)], 1450, 400, :silent, :large, #{:fire :poison}, #{}, #{:poisonous :demon :follows :hostile :nasty :collect :infravisible :infravision})
  (MonsterType. "ice devil", \&, :white, 11, 6, -4, 55, -12, #{:hell-only :no-corpse}, [(MonsterAttack. :claw :physical 1 4) (MonsterAttack. :claw :physical 1 4) (MonsterAttack. :bite :physical 2 4) (MonsterAttack. :sting :cold 3 4)], 1450, 400, :silent, :large, #{:fire :cold :poison}, #{}, #{:see-invis :poisonous :demon :follows :hostile :nasty :infravisible :infravision})
  (MonsterType. "nalfeshnee", \&, :red, 11, 9, -1, 65, -11, #{:hell-only :no-corpse}, [(MonsterAttack. :claw :physical 1 4) (MonsterAttack. :claw :physical 1 4) (MonsterAttack. :bite :physical 2 4) (MonsterAttack. :magic :spell 0 0)], 1450, 400, :spell, :large, #{:fire :poison}, #{}, #{:humanoid :poisonous :demon :follows :hostile :nasty :infravisible :infravision})
  (MonsterType. "pit fiend", \&, :red, 13, 6, -3, 65, -13, #{:hell-only :no-corpse}, [(MonsterAttack. :weapon :physical 4 2) (MonsterAttack. :weapon :physical 4 2) (MonsterAttack. :hug :physical 2 4)], 1450, 400, :growl, :large, #{:fire :poison}, #{}, #{:see-invis :poisonous :demon :follows :hostile :nasty :collect :infravisible :infravision})
  (MonsterType. "balrog", \&, :red, 16, 5, -2, 75, -14, #{:hell-only :no-corpse}, [(MonsterAttack. :weapon :physical 8 4) (MonsterAttack. :weapon :physical 4 6)], 1450, 400, :silent, :large, #{:fire :poison}, #{}, #{:fly :see-invis :poisonous :demon :follows :hostile :strong :nasty :collect :infravisible :infravision})
  (MonsterType. "Juiblex", \&, :bright-green, 50, 3, -7, 65, -15, #{:hell-only :no-corpse :not-generated :unique}, [(MonsterAttack. :engulf :disease 4 10) (MonsterAttack. :spit :acid 3 6)], 1500, 0, :gurgle, :large, #{:fire :poison :acid :stone}, #{}, #{:amphibious :amorphous :nohead :fly :see-invis :acid :poisonous :nopoly :demon :follows :hostile :proper-name :nasty :lord :male :waits :wants-amulet :infravision})
  (MonsterType. "Yeenoghu", \&, :magenta, 56, 18, -5, 80, -15, #{:hell-only :no-corpse :not-generated :unique}, [(MonsterAttack. :weapon :physical 3 6) (MonsterAttack. :weapon :conf 2 8) (MonsterAttack. :claw :paralysis 1 6) (MonsterAttack. :magic :magic-missile 2 6)], 900, 500, :grunt, :large, #{:fire :poison}, #{}, #{:fly :see-invis :poisonous :nopoly :demon :follows :hostile :proper-name :nasty :lord :male :collect :wants-amulet :infravisible :infravision})
  (MonsterType. "Orcus", \&, :magenta, 66, 9, -6, 85, -20, #{:hell-only :no-corpse :not-generated :unique}, [(MonsterAttack. :weapon :physical 3 6) (MonsterAttack. :claw :physical 3 4) (MonsterAttack. :claw :physical 3 4) (MonsterAttack. :magic :spell 8 6) (MonsterAttack. :sting :poison 2 4)], 1500, 500, :grunt, :huge, #{:fire :poison}, #{}, #{:fly :see-invis :poisonous :nopoly :demon :follows :hostile :proper-name :nasty :prince :male :collect :waits :wants-book :wants-amulet :infravisible :infravision})
  (MonsterType. "Geryon", \&, :magenta, 72, 3, -3, 75, 15, #{:hell-only :no-corpse :not-generated :unique}, [(MonsterAttack. :claw :physical 3 6) (MonsterAttack. :claw :physical 3 6) (MonsterAttack. :sting :poison 2 4)], 1500, 500, :bribe, :huge, #{:fire :poison}, #{}, #{:fly :see-invis :poisonous :slithy :nopoly :demon :follows :hostile :proper-name :nasty :prince :male :wants-amulet :infravisible :infravision})
  (MonsterType. "Dispater", \&, :magenta, 78, 15, -2, 80, 15, #{:hell-only :no-corpse :not-generated :unique}, [(MonsterAttack. :weapon :physical 4 6) (MonsterAttack. :magic :spell 6 6)], 1500, 500, :bribe, :human, #{:fire :poison}, #{}, #{:fly :see-invis :poisonous :humanoid :nopoly :demon :follows :hostile :proper-name :nasty :prince :male :collect :wants-amulet :infravisible :infravision})
  (MonsterType. "Baalzebub", \&, :magenta, 89, 9, -5, 85, 20, #{:hell-only :no-corpse :not-generated :unique}, [(MonsterAttack. :bite :poison 2 6) (MonsterAttack. :gaze :stun 2 6)], 1500, 500, :bribe, :large, #{:fire :poison}, #{}, #{:fly :see-invis :poisonous :nopoly :demon :follows :hostile :proper-name :nasty :prince :male :wants-amulet :waits :infravisible :infravision})
  (MonsterType. "Asmodeus", \&, :magenta, 105, 12, -7, 90, 20, #{:hell-only :no-corpse :not-generated :unique}, [(MonsterAttack. :claw :physical 4 4) (MonsterAttack. :magic :cold 6 6)], 1500, 500, :bribe, :huge, #{:fire :cold :poison}, #{}, #{:fly :see-invis :humanoid :poisonous :nopoly :demon :follows :hostile :proper-name :strong :nasty :prince :male :wants-amulet :waits :infravisible :infravision})
  (MonsterType. "Demogorgon", \&, :magenta, 106, 15, -8, 95, -20, #{:hell-only :no-corpse :not-generated :unique}, [(MonsterAttack. :magic :spell 8 6) (MonsterAttack. :sting :drain-xp 1 4) (MonsterAttack. :claw :disease 1 6) (MonsterAttack. :claw :disease 1 6)], 1500, 500, :growl, :huge, #{:fire :poison}, #{}, #{:fly :see-invis :nohands :poisonous :nopoly :demon :follows :hostile :proper-name :nasty :prince :male :wants-amulet :infravisible :infravision})
  (MonsterType. "Death", \&, :magenta, 30, 12, -5, 100, 0, #{:unique :not-generated}, [(MonsterAttack. :touch :death 8 8) (MonsterAttack. :touch :death 8 8)], 1450, 1, :rider, :human, #{:elbereth :fire :cold :shock :sleep :poison :stone}, #{}, #{:fly :humanoid :regen :see-invis :telecontrol :nopoly :follows :hostile :proper-name :strong :nasty :infravisible :infravision})
  (MonsterType. "Pestilence", \&, :magenta, 30, 12, -5, 100, 0, #{:unique :not-generated}, [(MonsterAttack. :touch :pestilence 8 8) (MonsterAttack. :touch :pestilence 8 8)], 1450, 1, :rider, :human, #{:elbereth :fire :cold :shock :sleep :poison :stone}, #{}, #{:fly :humanoid :regen :see-invis :telecontrol :nopoly :follows :hostile :proper-name :strong :nasty :infravisible :infravision})
  (MonsterType. "Famine", \&, :magenta, 30, 12, -5, 100, 0, #{:unique :not-generated}, [(MonsterAttack. :touch :famine 8 8) (MonsterAttack. :touch :famine 8 8)], 1450, 1, :rider, :human, #{:elbereth :fire :cold :shock :sleep :poison :stone}, #{}, #{:fly :humanoid :regen :see-invis :telecontrol :nopoly :follows :hostile :proper-name :strong :nasty :infravisible :infravision})
  (MonsterType. "djinni", \&, :yellow, 7, 12, 4, 30, 0, #{:not-generated :no-corpse}, [(MonsterAttack. :weapon :physical 2 8)], 1500, 400, :djinni, :human, #{:poison :stone}, #{}, #{:humanoid :fly :poisonous :nopoly :follows :collect :infravisible})
  (MonsterType. "sandestin", \&, nil, 13, 12, 4, 60, -5, #{:hell-only :no-corpse}, [(MonsterAttack. :weapon :physical 2 6) (MonsterAttack. :weapon :physical 2 6)], 1500, 400, :cuss, :human, #{:stone}, #{}, #{:humanoid :nopoly :follows :strong :collect :infravisible :infravision})
  (MonsterType. "jellyfish", \;, :blue, 3, 3, 6, 0, 0, #{:genocidable :not-generated}, [(MonsterAttack. :sting :poison 3 3)], 80, 20, :silent, :small, #{:poison}, #{:poison}, #{:swim :amphibious :slithy :nolimbs :notake :poisonous :hostile})
  (MonsterType. "piranha", \;, :red, 5, 12, 4, 0, 0, #{:genocidable :not-generated :sgroup}, [(MonsterAttack. :bite :physical 2 6)], 60, 30, :silent, :small, #{}, #{}, #{:swim :amphibious :animal :slithy :nolimbs :carnivore :oviparous :notake :hostile})
  (MonsterType. "shark", \;, nil, 7, 12, 2, 0, 0, #{:genocidable :not-generated}, [(MonsterAttack. :bite :physical 5 6)], 500, 350, :silent, :large, #{}, #{}, #{:swim :amphibious :animal :slithy :nolimbs :carnivore :oviparous :thick-hide :notake :hostile})
  (MonsterType. "giant eel", \;, :cyan, 5, 9, -1, 0, 0, #{:genocidable :not-generated}, [(MonsterAttack. :bite :physical 3 6) (MonsterAttack. :touch :wrap 0 0)], 200, 250, :silent, :huge, #{}, #{}, #{:swim :amphibious :animal :slithy :nolimbs :carnivore :oviparous :notake :hostile :infravisible})
  (MonsterType. "electric eel", \;, :bright-blue, 7, 10, -3, 0, 0, #{:genocidable :not-generated}, [(MonsterAttack. :bite :shock 4 6) (MonsterAttack. :touch :wrap 0 0)], 200, 250, :silent, :huge, #{:shock}, #{:shock}, #{:swim :amphibious :animal :slithy :nolimbs :carnivore :oviparous :notake :hostile :infravisible})
  (MonsterType. "kraken", \;, :red, 20, 3, 6, 0, -3, #{:genocidable :not-generated}, [(MonsterAttack. :claw :physical 2 4) (MonsterAttack. :claw :physical 2 4) (MonsterAttack. :hug :wrap 2 6) (MonsterAttack. :bite :physical 5 4)], 1800, 1000, :silent, :huge, #{}, #{}, #{:swim :amphibious :animal :nohands :carnivore :nopoly :hostile :strong :infravisible})
  (MonsterType. "newt", \:, :yellow, 0, 6, 8, 0, 0, #{:genocidable}, [(MonsterAttack. :bite :physical 1 2)], 10, 20, :silent, :tiny, #{}, #{}, #{:swim :amphibious :animal :nohands :carnivore :hostile})
  (MonsterType. "gecko", \:, :green, 1, 6, 8, 0, 0, #{:genocidable}, [(MonsterAttack. :bite :physical 1 3)], 10, 20, :sqeek, :tiny, #{}, #{}, #{:animal :nohands :carnivore :hostile})
  (MonsterType. "iguana", \:, :brown, 2, 6, 7, 0, 0, #{:genocidable}, [(MonsterAttack. :bite :physical 1 4)], 30, 30, :silent, :tiny, #{}, #{}, #{:animal :nohands :carnivore :hostile})
  (MonsterType. "baby crocodile", \:, :brown, 3, 6, 7, 0, 0, #{:genocidable}, [(MonsterAttack. :bite :physical 1 4)], 200, 200, :silent, :medium, #{}, #{}, #{:swim :amphibious :animal :nohands :carnivore :hostile})
  (MonsterType. "lizard", \:, :green, 5, 6, 6, 10, 0, #{:genocidable}, [(MonsterAttack. :bite :physical 1 6)], 10, 40, :silent, :tiny, #{:stone}, #{}, #{:animal :nohands :carnivore :hostile})
  (MonsterType. "chameleon", \:, :brown, 6, 5, 6, 10, 0, #{:genocidable}, [(MonsterAttack. :bite :physical 4 2)], 100, 100, :silent, :tiny, #{}, #{}, #{:animal :nohands :carnivore :nopoly :hostile})
  (MonsterType. "crocodile", \:, :brown, 6, 9, 5, 0, 0, #{:genocidable}, [(MonsterAttack. :bite :physical 4 2) (MonsterAttack. :claw :physical 1 12)], 1450, 400, :silent, :large, #{}, #{}, #{:swim :amphibious :animal :thick-hide :nohands :oviparous :carnivore :strong :hostile})
  (MonsterType. "salamander", \:, :orange, 8, 12, -1, 0, -9, #{:hell-only}, [(MonsterAttack. :weapon :physical 2 8) (MonsterAttack. :touch :fire 1 6) (MonsterAttack. :hug :physical 2 6) (MonsterAttack. :hug :fire 3 6)], 1500, 400, :mumble, :human, #{:sleep :fire}, #{:fire}, #{:humanoid :slithy :thick-hide :poisonous :follows :hostile :collect :picks-magic-items :infravisible})
  (MonsterType. "long worm tail", \~, :brown, 0, 0, 0, 0, 0, #{:not-generated :no-corpse :unique}, [], 0, 0, 0, 0, #{}, #{}, #{:nopoly :hostile})
  (MonsterType. "archeologist", \@, :white, 10, 12, 10, 1, 3, #{:not-generated}, [(MonsterAttack. :weapon :physical 1 6) (MonsterAttack. :weapon :physical 1 6)], 1450, 400, :humanoid, :human, #{:elbereth}, #{}, #{:humanoid :tunnel :digger :omnivore :nopoly :human :strong :collect :infravisible})
  (MonsterType. "barbarian", \@, :white, 10, 12, 10, 1, 0, #{:not-generated}, [(MonsterAttack. :weapon :physical 1 6) (MonsterAttack. :weapon :physical 1 6)], 1450, 400, :humanoid, :human, #{:elbereth :poison}, #{}, #{:humanoid :omnivore :nopoly :human :strong :collect :infravisible})
  (MonsterType. "caveman", \@, :white, 10, 12, 10, 0, 1, #{:not-generated}, [(MonsterAttack. :weapon :physical 2 4)], 1450, 400, :humanoid, :human, #{:elbereth}, #{}, #{:humanoid :omnivore :nopoly :human :strong :male :collect :infravisible})
  (MonsterType. "cavewoman", \@, :white, 10, 12, 10, 0, 1, #{:not-generated}, [(MonsterAttack. :weapon :physical 2 4)], 1450, 400, :humanoid, :human, #{:elbereth}, #{}, #{:humanoid :omnivore :nopoly :human :strong :female :collect :infravisible})
  (MonsterType. "healer", \@, :white, 10, 12, 10, 1, 0, #{:not-generated}, [(MonsterAttack. :weapon :physical 1 6)], 1450, 400, :humanoid, :human, #{:elbereth :poison}, #{}, #{:humanoid :omnivore :nopoly :human :strong :collect :infravisible})
  (MonsterType. "knight", \@, :white, 10, 12, 10, 1, 3, #{:not-generated}, [(MonsterAttack. :weapon :physical 1 6) (MonsterAttack. :weapon :physical 1 6)], 1450, 400, :humanoid, :human, #{:elbereth}, #{}, #{:humanoid :omnivore :nopoly :human :strong :collect :infravisible})
  (MonsterType. "monk", \@, :white, 10, 12, 10, 2, 0, #{:not-generated}, [(MonsterAttack. :claw :physical 1 8) (MonsterAttack. :kick :physical 1 8)], 1450, 400, :humanoid, :human, #{:elbereth}, #{}, #{:humanoid :herbivore :nopoly :human :strong :collect :male :infravisible})
  (MonsterType. "priest", \@, :white, 10, 12, 10, 2, 0, #{:not-generated}, [(MonsterAttack. :weapon :physical 1 6)], 1450, 400, :humanoid, :human, #{:elbereth}, #{}, #{:humanoid :omnivore :nopoly :human :strong :male :collect :infravisible})
  (MonsterType. "priestess", \@, :white, 10, 12, 10, 2, 0, #{:not-generated}, [(MonsterAttack. :weapon :physical 1 6)], 1450, 400, :humanoid, :human, #{:elbereth}, #{}, #{:humanoid :omnivore :nopoly :human :strong :female :collect :infravisible})
  (MonsterType. "ranger", \@, :white, 10, 12, 10, 2, -3, #{:not-generated}, [(MonsterAttack. :weapon :physical 1 4)], 1450, 400, :humanoid, :human, #{:elbereth}, #{}, #{:humanoid :omnivore :nopoly :human :strong :collect :infravisible})
  (MonsterType. "rogue", \@, :white, 10, 12, 10, 1, -3, #{:not-generated}, [(MonsterAttack. :weapon :physical 1 6) (MonsterAttack. :weapon :physical 1 6)], 1450, 400, :humanoid, :human, #{:elbereth}, #{}, #{:humanoid :omnivore :nopoly :human :strong :picks-gold :picks-jewels :collect :infravisible})
  (MonsterType. "samurai", \@, :white, 10, 12, 10, 1, 3, #{:not-generated}, [(MonsterAttack. :weapon :physical 1 8) (MonsterAttack. :weapon :physical 1 8)], 1450, 400, :humanoid, :human, #{:elbereth}, #{}, #{:humanoid :omnivore :nopoly :human :strong :collect :infravisible})
  (MonsterType. "tourist", \@, :white, 10, 12, 10, 1, 0, #{:not-generated}, [(MonsterAttack. :weapon :physical 1 6)], 1450, 400, :humanoid, :human, #{:elbereth :poison}, #{}, #{:humanoid :omnivore :nopoly :human :strong :collect :infravisible})
  (MonsterType. "valkyrie", \@, :white, 10, 12, 10, 1, -1, #{:not-generated}, [(MonsterAttack. :weapon :physical 1 8) (MonsterAttack. :weapon :physical 1 8)], 1450, 400, :humanoid, :human, #{:elbereth :cold}, #{}, #{:humanoid :omnivore :nopoly :human :strong :female :collect :infravisible})
  (MonsterType. "wizard", \@, :white, 10, 12, 10, 3, 0, #{:not-generated}, [(MonsterAttack. :weapon :physical 1 6)], 1450, 400, :humanoid, :human, #{:elbereth}, #{}, #{:humanoid :omnivore :nopoly :human :strong :collect :picks-magic-items :infravisible})
  (MonsterType. "Lord Carnarvon", \@, :magenta, 20, 12, 0, 30, 20, #{:not-generated :unique}, [(MonsterAttack. :weapon :physical 1 6)], 1450, 400, :leader, :human, #{:elbereth}, #{}, #{:tunnel :digger :humanoid :omnivore :nopoly :human :proper-name :peaceful :strong :male :collect :picks-magic-items :approach :infravisible})
  (MonsterType. "Pelias", \@, :magenta, 20, 12, 0, 30, 0, #{:not-generated :unique}, [(MonsterAttack. :weapon :physical 1 6)], 1450, 400, :leader, :human, #{:elbereth :poison}, #{}, #{:humanoid :omnivore :nopoly :human :proper-name :peaceful :strong :male :collect :picks-magic-items :approach :infravisible})
  (MonsterType. "Shaman Karnov", \@, :magenta, 20, 12, 0, 30, 20, #{:not-generated :unique}, [(MonsterAttack. :weapon :physical 2 4)], 1450, 400, :leader, :human, #{:elbereth}, #{}, #{:humanoid :omnivore :nopoly :human :proper-name :peaceful :strong :male :collect :picks-magic-items :approach :infravisible})
  (MonsterType. "Hippocrates", \@, :magenta, 20, 12, 0, 40, 0, #{:not-generated :unique}, [(MonsterAttack. :weapon :physical 1 6)], 1450, 400, :leader, :human, #{:elbereth :poison}, #{}, #{:humanoid :omnivore :nopoly :human :proper-name :peaceful :strong :male :collect :picks-magic-items :approach :infravisible})
  (MonsterType. "King Arthur", \@, :magenta, 20, 12, 0, 40, 20, #{:not-generated :unique}, [(MonsterAttack. :weapon :physical 1 6) (MonsterAttack. :weapon :physical 1 6)], 1450, 400, :leader, :human, #{:elbereth}, #{}, #{:humanoid :omnivore :nopoly :human :proper-name :peaceful :strong :male :collect :picks-magic-items :approach :infravisible})
  (MonsterType. "Grand Master", \@, :blue, 25, 12, 0, 70, 0, #{:not-generated :unique}, [(MonsterAttack. :claw :physical 4 10) (MonsterAttack. :kick :physical 2 8) (MonsterAttack. :magic :clerical 2 8) (MonsterAttack. :magic :clerical 2 8)], 1450, 400, :leader, :human, #{:elbereth :fire :shock :sleep :poison}, #{}, #{:humanoid :see-invis :herbivore :nopoly :human :peaceful :strong :nasty :picks-magic-items :approach :infravisible})
  (MonsterType. "Arch Priest", \@, :white, 25, 12, 7, 70, 0, #{:not-generated :unique}, [(MonsterAttack. :weapon :physical 4 10) (MonsterAttack. :kick :physical 2 8) (MonsterAttack. :magic :clerical 2 8) (MonsterAttack. :magic :clerical 2 8)], 1450, 400, :leader, :human, #{:elbereth :fire :shock :sleep :poison}, #{}, #{:humanoid :see-invis :omnivore :nopoly :human :peaceful :strong :collect :picks-magic-items :approach :infravisible})
  (MonsterType. "Orion", \@, :magenta, 20, 12, 0, 30, 0, #{:not-generated :unique}, [(MonsterAttack. :weapon :physical 1 6)], 1450, 400, :leader, :human, #{:elbereth}, #{}, #{:humanoid :omnivore :see-invis :swim :amphibious :nopoly :human :proper-name :peaceful :strong :male :collect :picks-magic-items :approach :infravision :infravisible})
  (MonsterType. "Master of Thieves", \@, :magenta, 20, 12, 0, 30, -20, #{:not-generated :unique}, [(MonsterAttack. :weapon :physical 2 6) (MonsterAttack. :weapon :physical 2 6) (MonsterAttack. :claw :steal-amulet 2 4)], 1450, 400, :leader, :human, #{:elbereth :stone}, #{}, #{:humanoid :omnivore :nopoly :human :peaceful :strong :male :picks-gold :picks-jewels :collect :picks-magic-items :approach :infravisible})
  (MonsterType. "Lord Sato", \@, :magenta, 20, 12, 0, 30, 20, #{:not-generated :unique}, [(MonsterAttack. :weapon :physical 1 8) (MonsterAttack. :weapon :physical 1 6)], 1450, 400, :leader, :human, #{:elbereth}, #{}, #{:humanoid :omnivore :nopoly :human :proper-name :peaceful :strong :male :collect :picks-magic-items :approach :infravisible})
  (MonsterType. "Norn", \@, :magenta, 20, 12, 0, 80, 0, #{:not-generated :unique}, [(MonsterAttack. :weapon :physical 1 8) (MonsterAttack. :weapon :physical 1 6)], 1450, 400, :leader, :human, #{:elbereth :cold}, #{}, #{:humanoid :omnivore :nopoly :human :peaceful :strong :female :collect :picks-magic-items :approach :infravisible})
  (MonsterType. "Neferet the Green", \@, :green, 20, 12, 0, 60, 0, #{:not-generated :unique}, [(MonsterAttack. :weapon :physical 1 6) (MonsterAttack. :magic :spell 2 8)], 1450, 400, :leader, :human, #{:elbereth}, #{}, #{:humanoid :omnivore :nopoly :human :female :proper-name :peaceful :strong :collect :picks-magic-items :approach :infravisible})
  (MonsterType. "Minion of Huhetotl", \&, :red, 16, 12, -2, 75, -14, #{:no-corpse :not-generated :unique}, [(MonsterAttack. :weapon :physical 8 4) (MonsterAttack. :weapon :physical 4 6) (MonsterAttack. :magic :spell 0 0) (MonsterAttack. :claw :steal-amulet 2 6)], 1450, 400, :nemesis, :large, #{:fire :poison :stone}, #{}, #{:fly :see-invis :poisonous :nopoly :demon :follows :hostile :strong :nasty :collect :wants-arti :waits :infravision :infravisible})
  (MonsterType. "Thoth Amon", \@, :magenta, 16, 12, 0, 10, -14, #{:not-generated :unique :no-corpse}, [(MonsterAttack. :weapon :physical 1 6) (MonsterAttack. :magic :spell 0 0) (MonsterAttack. :magic :spell 0 0) (MonsterAttack. :claw :steal-amulet 1 4)], 1450, 400, :nemesis, :human, #{:elbereth :poison :stone}, #{}, #{:humanoid :omnivore :nopoly :human :proper-name :strong :male :follows :hostile :nasty :collect :picks-magic-items :wants-arti :waits :infravisible})
  (MonsterType. "Chromatic Dragon", \D, :magenta, 16, 12, 0, 30, -14, #{:not-generated :unique}, [(MonsterAttack. :breath :breath 6 8) (MonsterAttack. :magic :spell 0 0) (MonsterAttack. :claw :steal-amulet 2 8) (MonsterAttack. :bite :physical 4 8) (MonsterAttack. :bite :physical 4 8) (MonsterAttack. :sting :physical 1 6)], 4500, 1700, :nemesis, :gigantic, #{:fire :cold :sleep :disintegration :shock :poison :acid :stone}, #{:fire :cold :sleep :disintegration :shock :poison }, #{:thick-hide :nohands :carnivore :see-invis :poisonous :nopoly :hostile :female :follows :strong :nasty :picks-gold :picks-jewels :picks-magic-items :wants-arti :waits :infravisible})
  (MonsterType. "Cyclops", \H, nil, 18, 12, 0, 0, -15, #{:not-generated :unique}, [(MonsterAttack. :weapon :physical 4 8) (MonsterAttack. :weapon :physical 4 8) (MonsterAttack. :claw :steal-amulet 2 6)], 1900, 700, :nemesis, :huge, #{:stone}, #{}, #{:humanoid :omnivore :nopoly :giant :strong :throws-boulders :follows :hostile :nasty :male :picks-jewels :collect :wants-arti :waits :infravision :infravisible :str})
  (MonsterType. "Ixoth", \D, :red, 15, 12, -1, 20, -14, #{:not-generated :unique}, [(MonsterAttack. :breath :fire 8 6) (MonsterAttack. :bite :physical 4 8) (MonsterAttack. :magic :spell 0 0) (MonsterAttack. :claw :physical 2 4) (MonsterAttack. :claw :steal-amulet 2 4)], 4500, 1600, :nemesis, :gigantic, #{:fire :stone}, #{:fire}, #{:fly :thick-hide :nohands :carnivore :see-invis :nopoly :proper-name :hostile :strong :nasty :follows :picks-gold :picks-jewels :picks-magic-items :wants-arti :waits :infravisible})
  (MonsterType. "Master Kaen", \@, :magenta, 25, 12, -10, 10, -20, #{:not-generated :unique}, [(MonsterAttack. :claw :physical 16 2) (MonsterAttack. :claw :physical 16 2) (MonsterAttack. :magic :clerical 0 0) (MonsterAttack. :claw :steal-amulet 1 4)], 1450, 400, :nemesis, :human, #{:elbereth :poison :stone}, #{:poison}, #{:humanoid :herbivore :see-invis :nopoly :human :proper-name :hostile :strong :nasty :follows :collect :picks-magic-items :wants-arti :waits :infravisible})
  (MonsterType. "Nalzok", \&, :red, 16, 12, -2, 85, -127, #{:not-generated :unique :no-corpse}, [(MonsterAttack. :weapon :physical 8 4) (MonsterAttack. :weapon :physical 4 6) (MonsterAttack. :magic :spell 0 0) (MonsterAttack. :claw :steal-amulet 2 6)], 1450, 400, :nemesis, :large, #{:fire :poison :stone}, #{}, #{:fly :see-invis :poisonous :nopoly :demon :proper-name :hostile :strong :follows :nasty :collect :wants-arti :waits :infravision :infravisible})
  (MonsterType. "Scorpius", \s, :magenta, 15, 12, 10, 0, -15, #{:not-generated :unique}, [(MonsterAttack. :claw :physical 2 6) (MonsterAttack. :claw :steal-amulet 2 6) (MonsterAttack. :sting :disease 1 4)], 750, 350, :nemesis, :human, #{:poison :stone}, #{:poison}, #{:animal :nohands :oviparous :poisonous :carnivore :nopoly :proper-name :hostile :strong :follows :nasty :collect :picks-magic-items :wants-arti :waits})
  (MonsterType. "Master Assassin", \@, :magenta, 15, 12, 0, 30, 18, #{:not-generated :unique}, [(MonsterAttack. :weapon :poison 2 6) (MonsterAttack. :weapon :physical 2 8) (MonsterAttack. :claw :steal-amulet 2 6)], 1450, 400, :nemesis, :human, #{:elbereth :stone}, #{}, #{:humanoid :omnivore :nopoly :human :strong :hostile :follows :nasty :collect :picks-magic-items :wants-arti :waits :infravisible})
  (MonsterType. "Ashikaga Takauji", \@, :magenta, 15, 12, 0, 40, -13, #{:not-generated :unique :no-corpse}, [(MonsterAttack. :weapon :physical 2 6) (MonsterAttack. :weapon :physical 2 6) (MonsterAttack. :claw :steal-amulet 2 6)], 1450, 400, :nemesis, :human, #{:elbereth :stone}, #{}, #{:humanoid :omnivore :nopoly :human :proper-name :hostile :strong :follows :nasty :male :collect :picks-magic-items :wants-arti :waits :infravisible})
  (MonsterType. "Lord Surtur", \H, :magenta, 15, 12, 2, 50, 12, #{:not-generated :unique}, [(MonsterAttack. :weapon :physical 2 10) (MonsterAttack. :weapon :physical 2 10) (MonsterAttack. :claw :steal-amulet 2 6)], 2250, 850, :nemesis, :huge, #{:fire :stone}, #{:fire}, #{:humanoid :omnivore :nopoly :giant :male :proper-name :hostile :follows :strong :nasty :throws-boulders :picks-jewels :collect :wants-arti :waits :infravision :infravisible :str})
  (MonsterType. "Dark One", \@, :blue, 15, 12, 0, 80, -10, #{:not-generated :unique :no-corpse}, [(MonsterAttack. :weapon :physical 1 6) (MonsterAttack. :weapon :physical 1 6) (MonsterAttack. :claw :steal-amulet 1 4) (MonsterAttack. :magic :spell 0 0)], 1450, 400, :nemesis, :human, #{:elbereth :stone}, #{}, #{:humanoid :omnivore :nopoly :human :strong :hostile :follows :nasty :collect :picks-magic-items :wants-arti :waits :infravisible})
  (MonsterType. "student", \@, :white, 5, 12, 10, 10, 3, #{:not-generated}, [(MonsterAttack. :weapon :physical 1 6)], 1450, 400, :guardian, :human, #{:elbereth}, #{}, #{:tunnel :digger :humanoid :omnivore :nopoly :human :peaceful :strong :collect :infravisible})
  (MonsterType. "chieftain", \@, :white, 5, 12, 10, 10, 0, #{:not-generated}, [(MonsterAttack. :weapon :physical 1 6)], 1450, 400, :guardian, :human, #{:elbereth :poison}, #{}, #{:humanoid :omnivore :nopoly :human :peaceful :strong :collect :infravisible})
  (MonsterType. "neanderthal", \@, :white, 5, 12, 10, 10, 1, #{:not-generated}, [(MonsterAttack. :weapon :physical 2 4)], 1450, 400, :guardian, :human, #{:elbereth}, #{}, #{:humanoid :omnivore :nopoly :human :peaceful :strong :collect :infravisible})
  (MonsterType. "attendant", \@, :white, 5, 12, 10, 10, 3, #{:not-generated}, [(MonsterAttack. :weapon :physical 1 6)], 1450, 400, :guardian, :human, #{:elbereth :poison}, #{}, #{:humanoid :omnivore :nopoly :human :peaceful :strong :collect :infravisible})
  (MonsterType. "page", \@, :white, 5, 12, 10, 10, 3, #{:not-generated}, [(MonsterAttack. :weapon :physical 1 6) (MonsterAttack. :weapon :physical 1 6)], 1450, 400, :guardian, :human, #{:elbereth}, #{}, #{:humanoid :omnivore :nopoly :human :peaceful :strong :collect :infravisible})
  (MonsterType. "abbot", \@, :white, 5, 12, 10, 20, 0, #{:not-generated}, [(MonsterAttack. :claw :physical 8 2) (MonsterAttack. :kick :stun 3 2) (MonsterAttack. :magic :clerical 0 0)], 1450, 400, :guardian, :human, #{:elbereth}, #{}, #{:humanoid :herbivore :nopoly :human :peaceful :strong :collect :infravisible})
  (MonsterType. "acolyte", \@, :white, 5, 12, 10, 20, 0, #{:not-generated}, [(MonsterAttack. :weapon :physical 1 6) (MonsterAttack. :magic :clerical 0 0)], 1450, 400, :guardian, :human, #{:elbereth}, #{}, #{:humanoid :omnivore :nopoly :human :peaceful :strong :collect :infravisible})
  (MonsterType. "hunter", \@, :white, 5, 12, 10, 10, -7, #{:not-generated}, [(MonsterAttack. :weapon :physical 1 4)], 1450, 400, :guardian, :human, #{:elbereth}, #{}, #{:humanoid :see-invis :omnivore :nopoly :human :peaceful :strong :collect :infravision :infravisible})
  (MonsterType. "thug", \@, :white, 5, 12, 10, 10, -3, #{:not-generated}, [(MonsterAttack. :weapon :physical 1 6) (MonsterAttack. :weapon :physical 1 6)], 1450, 400, :guardian, :human, #{:elbereth}, #{}, #{:humanoid :omnivore :nopoly :human :peaceful :strong :picks-gold :collect :infravisible})
  (MonsterType. "ninja", \@, :white, 5, 12, 10, 10, 3, #{:not-generated}, [(MonsterAttack. :weapon :physical 1 8) (MonsterAttack. :weapon :physical 1 8)], 1450, 400, :humanoid, :human, #{:elbereth}, #{}, #{:humanoid :omnivore :nopoly :human :hostile :strong :collect :infravisible})
  (MonsterType. "roshi", \@, :white, 5, 12, 10, 10, 3, #{:not-generated}, [(MonsterAttack. :weapon :physical 1 8) (MonsterAttack. :weapon :physical 1 8)], 1450, 400, :guardian, :human, #{:elbereth}, #{}, #{:humanoid :omnivore :nopoly :human :peaceful :strong :collect :infravisible})
  (MonsterType. "warrior", \@, :white, 5, 12, 10, 10, -1, #{:not-generated}, [(MonsterAttack. :weapon :physical 1 8) (MonsterAttack. :weapon :physical 1 8)], 1450, 400, :guardian, :human, #{:elbereth}, #{}, #{:humanoid :omnivore :nopoly :human :peaceful :strong :collect :female :infravisible})
  (MonsterType. "apprentice", \@, :white, 5, 12, 10, 30, 0, #{:not-generated}, [(MonsterAttack. :weapon :physical 1 6) (MonsterAttack. :magic :spell 0 0)], 1450, 400, :guardian, :human, #{:elbereth}, #{}, #{:humanoid :omnivore :nopoly :human :peaceful :strong :collect :picks-magic-items :infravisible})
  (MonsterType. "gremlin", \g, :green, 5, 12, 2, 25, -9, #{:genocidable}, [(MonsterAttack. :claw :physical 1 6) (MonsterAttack. :claw :physical 1 6) (MonsterAttack. :bite :physical 1 4) (MonsterAttack. :claw :curse 0 0)], 100, 20, :laugh, :small, #{:poison}, #{:poison}, #{:swim :humanoid :poisonous :follows :infravisible})
  (MonsterType. "gargoyle", \g, :brown, 6, 10, -4, 0, -9, #{:genocidable}, [(MonsterAttack. :claw :physical 2 6) (MonsterAttack. :claw :physical 2 6) (MonsterAttack. :bite :physical 2 4)], 1000, 200, :grunt, :human, #{:stone}, #{}, #{:humanoid :thick-hide :breathless :hostile :strong :oviparous})
  (MonsterType. "mail daemon", \&, :bright-blue, 8, 12, -4, 30, -7, #{:no-corpse :not-generated}, [(MonsterAttack. :weapon :physical 1 3) (MonsterAttack. :claw :physical 1 3) (MonsterAttack. :bite :physical 1 3)], 1450, 400, :djinni, :human, #{:fire :poison}, #{}, #{:humanoid :poisonous :swim :nopoly :demon :follows :fly :peaceful :infravisible :infravision})])

(def shopkeepers
  #{"Abisko" "Abitibi" "Adjama" "Akalapi" "Akhalataki" "Aklavik"
    "Akranes" "Aksaray" "Akureyri" "Alaca" "Aned" "Angmagssalik"
    "Annootok" "Ardjawinangun" "Artvin" "Asidonhopo" "Avasaksa" "Ayancik"
    "Babadag" "Baliga" "Ballingeary" "Balya" "Bandjar" "Banjoewangi"
    "Bayburt" "Beddgelert" "Beinn a Ghlo" "Berbek" "Berhala" "Bicaz"
    "Birecik" "Bnowr Falr" "Bojolali" "Bordeyri" "Boyabai" "Braemar"
    "Brienz" "Brig" "Brzeg" "Budereyri" "Burglen" "Caergybi"
    "Cahersiveen" "Cannich" "Carignan" "Cazelon" "Chibougamau"
    "Chicoutimi" "Cire Htims" "Clonegal" "Corignac" "Corsh" "Cubask"
    "Culdaff" "Curig" "Demirci" "Dirk" "Djasinga" "Djombang"
    "Dobrinishte" "Donmyar" "Dorohoi" "Droichead Atha" "Drumnadrochit"
    "Dunfanaghy" "Dunvegan" "Eauze" "Echourgnac" "Eed-morra" "Eforie"
    "Ekim-p" "Elm" "Enniscorthy" "Ennistymon" "Enontekis" "Enrobwem"
    "Ermenak" "Erreip" "Ettaw-noj" "Evad'kh" "Eygurande" "Eymoutiers"
    "Eypau" "Falo" "Fauske" "Fenouilledes" "Fetesti" "Feyfer" "Fleac"
    "Flims" "Flugi" "Gairloch" "Gaziantep" "Gellivare" "Gheel"
    "Glenbeigh" "Gliwice" "Gomel" "Gorlowka" "Guizengeard" "Gweebarra"
    "Haparanda" "Haskovo" "Havic" "Haynin" "Hebiwerie" "Hoboken"
    "Holmavik" "Hradec Kralove" "Htargcm" "Hyeghu" "Imbyze" "Inishbofin"
    "Inniscrone" "Inuvik" "Inverurie" "Iskenderun" "Ivrajimsal" "Izchak"
    "Jiu" "Jonzac" "Jumilhac" "Juyn" "Kabalebo" "Kachzi Rellim"
    "Kadirli" "Kahztiy" "Kajaani" "Kalecik" "Kanturk" "Karangkobar"
    "Kars" "Kautekeino" "Kediri" "Kerloch" "Kesh" "Kilgarvan" "Kilmihil"
    "Kiltamagh" "Kinnegad" "Kinojevis" "Kinsky" "Kipawa" "Kirikkale"
    "Kirklareli" "Kittamagh" "Kivenhoug" "Klodzko" "Konosja" "Kopasker"
    "Krnov" "Kyleakin" "Kyzyl" "Labouheyre" "Laguiolet" "Lahinch" "Lapu"
    "Lechaim" "Lerignac" "Leuk" "Lexa" "Lez-tneg" "Liorac" "Lisnaskea"
    "Llandrindod" "Llanerchymedd" "Llanfair-ym-muallt" "Llanrwst"
    "Lochnagar" "Lom" "Lonzac" "Lovech" "Lucrezia" "Ludus"
    "Lugnaquillia" "Lulea" "Maesteg" "Maganasipi" "Makharadze" "Makin"
    "Malasgirt" "Malazgirt" "Mallwyd" "Mamaia" "Manlobbi" "Massis"
    "Matagami" "Matray" "Melac" "Midyat" "Monbazillac" "Morven" "Moy"
    "Mron" "Mured-oog" "Nairn" "Nallihan" "Narodnaja" "Nehoiasu"
    "Nehpets" "Nenagh" "Nenilukah" "Neuvicq" "Ngebel" "Nhoj-lee" "Nieb"
    "Niknar" "Niod" "Nisipitu" "Niskal" "Nivram" "Njalindoeng" "Njezjin"
    "Nosalnef" "Nosid-da'r" "Noskcirdneh" "Noslo" "Nosnehpets" "Oeloe"
    "Olycan" "Oryahovo" "Ossipewsk" "Ouiatchouane" "Pakka Pakka"
    "Pameunpeuk" "Panagyuritshte" "Papar" "Parbalingga" "Pasawahan"
    "Patjitan" "Pemboeang" "Pengalengan" "Pernik" "Pervari" "Picq"
    "Polatli" "Pons" "Pontarfynach" "Possogroenoe" "Queyssac" "Raciborz"
    "Rastegaisa" "Rath Luirc" "Razboieni" "Rebrol-nek" "Regien" "Rellenk"
    "Renrut" "Rewuorb" "Rhaeader" "Rhydaman" "Rouffiac" "Rovaniemi"
    "Sablja" "Sadelin" "Samoe" "Sarangan" "Sarnen" "Saujon" "Schuls"
    "Semai" "Sgurr na Ciche" "Siboga" "Sighisoara" "Siirt" "Silistra"
    "Sipaliwini" "Siverek" "Skibbereen" "Slanic" "Sliven" "Smolyan"
    "Sneem" "Snivek" "Sperc" "Stewe" "Storr" "Svaving" "Swidnica"
    "Syktywkar" "Tapper" "Tefenni" "Tegal" "Telloc Cyaj" "Terwen" "Thun"
    "Tipor" "Tirebolu" "Tirgu Neamt" "Tjibarusa" "Tjisolok" "Tjiwidej"
    "Tonbar" "Touverac" "Trahnil" "Trallwng" "Trenggalek" "Tringanoe"
    "Troyan" "Tsew-mot" "Tsjernigof" "Tuktoyaktuk" "Tulovo" "Turriff"
    "Uist" "Upernavik" "Urignac" "Vals" "Vanzac" "Varjag Njarga"
    "Varvara" "Vaslui" "Vergt" "Voulgezac" "Walbrzych" "Weliki Oestjoeg"
    "Wirix" "Wonotobo" "Y-Fenni" "Y-crad" "Yad" "Yao-hang" "Yawolloh"
    "Ydna-s" "Yelpur" "Yildizeli" "Yl-rednow" "Ymla" "Ypey" "Yr Wyddgrug"
    "Ytnu-haled" "Zarnesti" "Zimnicea" "Zlatna" "Zlaw" "Zonguldak" "Zum Loch"})

(def role-ranks
  {"archeologist" #{"digger" "field worker" "investigator" "exhumer" "excavator" "spelunker" "speleologist" "collector" "curator"}
   "barbarian" #{"plunderer" "plunderess" "pillager" "bandit" "brigand" "raider" "reaver" "slayer" "chieftain" "chieftainess" "conqueror" "conqueress"}
   "caveman" #{"cavewoman" "troglodyte" "aborigine" "wanderer" "vagrant" "wayfarer" "roamer" "nomad" "rover" "pioneer"}
   "healer" #{"rhizotomist" "empiric" "embalmer" "dresser" "medicus ossium" "medica ossium" "herbalist" "magister" "magistra" "physician" "chirurgeon"}
   "knight" #{"gallant" "esquire" "bachelor" "sergeant" "knight" "banneret" "chevalier" "chevaliere" "seignieur" "dame" "paladin"}
   "monk" #{"candidate" "novice" "initiate" "student of stones" "student of waters" "student of metals" "student of winds" "student of fire" "master"}
   "priest" #{"priestess" "aspirant" "acolyte" "adept" "priest" "curate" "canon" "canoness" "lama" "patriarch"}
   "rogue" #{"footpad" "cutpurse" "rogue" "pilferer" "robber" "burglar" "filcher" "magsman" "magswoman" "thief"}
   "ranger" #{"tenderfoot" "lookout" "trailblazer" "reconnoiterer" "reconnoiteress" "scout" "arbalester" "archer" "sharpshooter" "marksman" "markswoman"}
   "samurai" #{"hatamoto" "ronin" "ninja" "kunoichi" "joshu" "ryoshu" "kokushu" "daimyo" "kuge" "shogun"}
   "tourist" #{"rambler" "sightseer" "excursionist" "peregrinator" "peregrinatrix" "traveler" "journeyer" "voyager" "explorer" "adventurer"}
   "valkyrie" #{"stripling" "skirmisher" "fighter" "man-at-arms" "woman-at-arms" "warrior" "swashbuckler" "hero" "heroine" "champion" "lord" "lady"}
   "wizard" #{"evoker" "conjurer" "thaumaturge" "magician" "enchanter" "enchantress" "sorcerer" "sorceress" "necromancer" "wizard" "mage"}})

(def appearance->monster ; {glyph => {color => MonsterType}}, only unambiguous
  (as-> {} m
    (reduce (fn [res monster]
              (update-in res [(:glyph monster) (:color monster)]
                         #(if % :ambiguous monster)))
            m monster-types)
    (into {} (for [glyph (keys m)]
               [glyph (into {} (remove (fn [[color monster]]
                                         (= :ambiguous monster))
                                       (m glyph)))]))
    (assoc-in m [\space nil] (get-in m [\X nil]))))

(def ^:private by-name-map ; {name => MonsterType}, also all-lowercase name variants
  (into {} (for [{:keys [name] :as m} monster-types
                 entry [[name m] [(string/lower-case name) m]]]
             entry)))

(def name->monster (comp by-name-map string/lower-case))

(def by-rank-map
  (reduce (fn [res [role ranks]]
            (reduce #(assoc %1 %2 role) res ranks))
          {} role-ranks))

(def rank->monster (comp by-rank-map string/lower-case))

(defn- strip-modifier [desc]
  (condp #(.startsWith ^String %2 %1) desc
    "invisible " (subs desc 10)
    "saddled " (subs desc 8)
    desc))

(defn- strip-disposition [desc]
  (condp #(.startsWith ^String %2 %1) ^String desc
    "tame " (subs desc 5)
    "peaceful " (subs desc 9)
    "guardian " (if (.contains desc " naga") desc (subs desc 9))
    desc))

(defn- strip-article [desc]
  (condp #(.startsWith ^String %2 %1) desc
    "a " (subs desc 2)
    "an " (subs desc 3)
    "the " (subs desc 4)
    "your " (subs desc 5)
    desc))

(defn by-description
  "Return MonsterType by farlook description"
  [text]
  (let [^String desc (-> text strip-article strip-disposition strip-modifier)
        ghost-or-called (re-seq #"ghost|called" desc)]
    (or (if (= "tail of a peaceful long worm" desc)
          (name->monster "long worm tail"))
        (if (re-seq #"^(?:the )?high priest(?:ess)?$" desc)
          (name->monster "high priest"))
        (if (= desc "mimic")
          (name->monster "large mimic")) ; could be any mimic really
        (if-let [[desc god] (and (not (re-seq #"Minion of Huhetotl| Yendor"
                                              desc))
                                 (not ghost-or-called)
                                 (re-first-groups #"(.*) of (.*)" desc))]
          (or (if (re-seq #"poohbah|priest|priestess" desc)
                (if (.contains desc "high ")
                  (name->monster "high priest")
                  (name->monster "aligned priest"))
                (if (.startsWith desc "guardian ")
                  (name->monster (subs desc 9))
                  (name->monster desc)))
              (throw (IllegalArgumentException.
                       (str "Failed to parse monster-of description: " text)))))
        (if-let [[nick desc] (and (not ghost-or-called)
                                  (not (.contains desc "Neferet the Green"))
                                  (not (.contains desc "Vlad the Impaler"))
                                  (re-first-groups #"(.*) the (.*)" desc))]
          (name->monster (strip-modifier desc)))
        (if-let [[desc nick] (re-first-groups #"(.*) called (.*)" desc)]
          (name->monster desc))
        (if (re-seq #"'?s? ghost" desc)
          (name->monster "ghost"))
        (if (.contains desc "coyote - ")
          (name->monster "coyote"))
        (if (shopkeepers desc)
          (name->monster "shopkeeper"))
        (name->monster desc)
        (rank->monster desc)
        (throw (IllegalArgumentException.
                 (str "Failed to parse monster description: " text))))))
