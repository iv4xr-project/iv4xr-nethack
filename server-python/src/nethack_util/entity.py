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

    object_infos = []
    for i in range(nethack.NUM_OBJECTS):
        object_info = nethack.objclass(i)
        object_dict = dict()
        object_dict["index"] = i
        object_dict["name"] = nethack.OBJ_NAME(object_info)
        object_dict["description"] = nethack.OBJ_DESCR(object_info)
        for property in filtered_properties:
            mapped_property = property
            if property == "oc_name_idx" or property == "oc_descr_idx" or property == "oc_oprop" or property == "oc_class" or property == "oc_delay" or property == "oc_color" or property == "oc_prob":
                continue

            if property == "oc_weight":
                mapped_property = "weight"
            elif property == "oc_cost":
                mapped_property = "cost"

            object_dict[mapped_property] = object_info.__getattribute__(property)

        object_infos.append(object_dict)

    return object_infos


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
