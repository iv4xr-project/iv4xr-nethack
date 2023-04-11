import json
import numpy as np
from nle import nethack


def filtered_object_properties():
    properties = nethack.objclass.__dict__
    filtered_properties = []
    for entry in properties:
        if type(properties[entry]) != property:
            continue

        filtered_properties.append(entry)

    return filtered_properties


def object_info():
    filtered_properties = filtered_object_properties()

    launchers = {21, 22, 23}

    object_infos = []
    for i in range(nethack.NUM_OBJECTS):
        object_info = nethack.objclass(i)
        object_dict = dict()
        object_dict["index"] = i
        object_dict["name"] = nethack.OBJ_NAME(object_info)
        object_dict["description"] = nethack.OBJ_DESCR(object_info)

        for property in filtered_properties:
            mapped_property = property
            if property == "oc_name_idx" or property == "oc_descr_idx" or property == "oc_oprop" or property == "oc_delay" or property == "oc_color" or property == "oc_prob":
                continue

            if property == "oc_weight":
                mapped_property = "weight"
            elif property == "oc_class":
                object_dict["entityClass"] = toEntityClass(int.from_bytes(object_info.oc_class[0].encode(), 'little'))
                continue
            elif property == "oc_cost":
                mapped_property = "cost"
            elif property == "oc_subtyp":
                value = object_info.__getattribute__(property)
                if int.from_bytes(object_info.oc_class[0].encode(), 'little') != 3:
                  abs_value = abs(value)
                  object_dict["skill"] = toSkill(abs_value)
                  object_dict["missile"] = value < 0
                  object_dict["fromLauncher"] = launchers.__contains__(abs_value)
                else:
                  object_dict["armorType"] = getArmor(value)
                continue


            object_dict[mapped_property] = object_info.__getattribute__(property)

        object_infos.append(object_dict)

    return object_infos


def toEntityClass(value: int):
  match value:
    case 0:
      return "RANDOM"
    case 1:
      return "ILLOBJ"
    case 2:
      return "WEAPON"
    case 3:
      return "ARMOR"
    case 4:
      return "RING"
    case 5:
      return "AMULET"
    case 6:
      return "TOOL"
    case 7:
      return "FOOD"
    case 8:
      return "POTION"
    case 9:
      return "SCROLL"
    case 10:
      return "SPELL_BOOK"
    case 11:
      return "WAND"
    case 12:
      return "COIN"
    case 13:
      return "GEM"
    case 14:
      return "ROCK"
    case 15:
      return "BALL"
    case 16:
      return "CHAIN"
    case 17:
      return "VENOM"


def getArmor(value: int):
    match value:
        case 0:
            return "SUIT"
        case 1:
            return "SHIELD"
        case 2:
            return "HELM"
        case 3:
            return "GLOVES"
        case 4:
            return "BOOTS"
        case 5:
            return "CLOAK"
        case 6:
            return "SHIRT"


def toSkill(value: int):
    match value:
        case 0:
            return "NONE"
        case 1:
            return "DAGGER"
        case 2:
            return "KNIFE"
        case 3:
            return "AXE"
        case 4:
            return "PICK_AXE"
        case 5:
            return "SHORT_SWORD"
        case 6:
            return "BROAD_SWORD"
        case 7:
            return "LONG_SWORD"
        case 8:
            return "TWO_HANDED_SWORD"
        case 9:
            return "SCIMITAR"
        case 10:
            return "SABER"
        case 11:
            return "CLUB"
        case 12:
            return "MACE"
        case 13:
            return "MORNING_STAR"
        case 14:
            return "FLAIL"
        case 15:
            return "HAMMER"
        case 16:
            return "QUARTERSTAFF"
        case 17:
            return "POLEARMS"
        case 18:
            return "SPEAR"
        case 19:
            return "TRIDENT"
        case 20:
            return "LANCE"
        case 21:
            return "BOW"
        case 22:
            return "SLING"
        case 23:
            return "CROSSBOW"
        case 24:
            return "DART"
        case 25:
            return "SHURIKEN"
        case 26:
            return "BOOMERANG"
        case 27:
            return "WHIP"
        case 28:
            return "UNICORN_HORN"
        case 29:
            return "ATTACK_SPELL"
        case 30:
            return "HEALING_SPELL"
        case 31:
            return "DIVINATION_SPELL"
        case 32:
            return "ENCHANTMENT_SPELL"
        case 33:
            return "CLERIC_SPELL"
        case 34:
            return "ESCAPE_SPELL"
        case 35:
            return "MATTER_SPELL"
        case 36:
            return "BARE_HANDED_COMBAT"
        case 37:
            return "TWO_WEAPON_COMBAT"
        case 38:
            return "RIDING"
        case 39:
            return "NUM_SKILLS"


def write_object_info():
    objects_info = object_info()

    # open a file for writing
    with open("../../data/entity.json", "w") as outfile:
        # use the json.dump() method to write the list of dictionaries to the file
        json.dump(objects_info, outfile, indent=2)


if __name__ == "__main__":
    write_object_info()

    glyph_arr = np.arange(nethack.MAX_GLYPH)
    obj_ind_arr = np.zeros(nethack.MAX_GLYPH)
    for i in glyph_arr:
        obj_ind_arr[i] = nethack.glyph_to_obj(i)
