package nethack.enums;

// Definitions found in lib/nle/include/skills.h
public enum Skill {
  NONE,

  /* Weapon Skills -- Stephen White
   * Order matters and are used in macros.
   * Positive values denote hand-to-hand weapons or launchers.
   * Negative values denote ammunition or missiles.
   * Update weapon.c if you amend any skills.
   * Also used for oc_subtyp.
   */
  DAGGER,
  KNIFE,
  AXE,
  PICK_AXE,
  SHORT_SWORD,
  BROAD_SWORD,
  LONG_SWORD,
  TWO_HANDED_SWORD,
  SCIMITAR,
  SABER,
  CLUB, /* Heavy-shafted bludgeon */
  MACE,
  MORNING_STAR, /* Spiked bludgeon */
  FLAIL, /* Two pieces hinged or chained together */
  HAMMER, /* Heavy head on the end */
  QUARTERSTAFF, /* Long-shafted bludgeon */
  POLEARMS, /* attack two or three steps away */
  SPEAR, /* includes javelin */
  TRIDENT,
  LANCE,
  BOW, /* launchers */
  SLING,
  CROSSBOW,
  DART, /* hand-thrown missiles */
  SHURIKEN,
  BOOMERANG,
  WHIP, /* flexible, one-handed */
  UNICORN_HORN, /* last weapon, two-handed */

  /* Spell Skills added by Larry Stewart-Zerba */
  ATTACK_SPELL,
  HEALING_SPELL,
  DIVINATION_SPELL,
  ENCHANTMENT_SPELL,
  CLERIC_SPELL,
  ESCAPE_SPELL,
  MATTER_SPELL,

  /* Other types of combat */
  BARE_HANDED_COMBAT, /* actually weaponless; gloves are ok */
  TWO_WEAPON_COMBAT, /* pair of weapons, one in each hand */
  RIDING, /* How well you control your steed */

  NUM_SKILLS;
}
