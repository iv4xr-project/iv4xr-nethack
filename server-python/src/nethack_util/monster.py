import re
import json
import numpy as np
from nle import nethack

from src.nethack_util import message, step

# Defined in lib/nle/include/display.h
# Can throw index out of range
# nethack.permonst(nethack.glyph_to_mon(np.array([1281])))

# nethack.glyph_is_monster(1500) returns true/false

# ID starts at 1
counter = 1
indexes = np.arange(0, 21 * 79).reshape(21, 79)


def glyph_to_is_monster():
    glyphs = np.arange(0, nethack.MAX_GLYPH + 1)
    func = np.vectorize(lambda g: nethack.glyph_is_monster(g))
    return func(glyphs)

MONSTER_GLYPHS = glyph_to_is_monster()


def id_monsters(env, obs):
    msg = message.read_obs_msg(obs)
    can_id_monsters = not msg.__contains__('(n)')

    is_monster = MONSTER_GLYPHS[obs['glyphs']]
    monster_indexes = indexes[is_monster]
    pattern = r"^.*(?:called|named)\s(\S+)"

    monster_descriptions = np.zeros((21, 79), dtype=int)
    descriptions = obs['screen_descriptions']
    id_monster = False
    for index in monster_indexes:
        x = index % 79
        y = int(index / 79)

        description: str = to_description(descriptions[y, x])
        match = re.search(pattern, description)

        if match:
            word = match.group(1)
            if word.isnumeric():
                value = int(word)
                monster_descriptions[y][x] = value
            continue

        # Cannot name a human since already has a name
        character = obs['chars'][y][x]
        if character == ord('@'):
            continue

        if can_id_monsters:
            print("Unidentified monster detected", x, y, "index:", index)
            obs, done = unique_id_monster(env, obs, x, y, monster_descriptions, index)
            id_monster = True

    if id_monster:
        env.render()

    return obs, monster_descriptions


def to_description(arr):
    func = np.vectorize(lambda t: chr(t))
    char_arr = func(arr)
    return ''.join(char_arr)


def move_cursor(env, key, delta):
    while int(delta / 8) != 0:
        step.step_stroke(env, key.capitalize())
        delta -= 8
    while delta != 0:
        step.step_stroke(env, key)
        delta -= 1


def unique_id_monster(env, obs, x, y, monster_descriptions, index):
    global counter
    cursor_x, cursor_y = get_cursor_pos(obs)

    # Activate command '#name'
    for char in '#n':
        step.step_stroke(env, char)
    step.step_stroke(env, '\n')

    # Name a monster
    step.step_stroke(env, 'm')

    # Move cursor to correct position
    dx = cursor_x - x
    x_key = 'l' if dx < 0 else 'h'
    dx = abs(dx)
    move_cursor(env, x_key, dx)

    dy = cursor_y - y
    y_key = 'j' if dy < 0 else 'k'
    dy = abs(dy)
    move_cursor(env, y_key, dy)

    # Select square
    obs, _ = step.step_stroke(env, ',')
    msg = message.read_obs_msg(obs)

    # Creature has no name yet
    if not msg.__contains__('called'):
        # Give name character by character
        for char in str(counter):
            step.step_stroke(env, char)

        print("NAME", counter)
        monster_descriptions[y][x] = counter
        # Increment counter
        counter += 1
    else:
        msg_words = msg.replace('?', '').split()
        monster_id = msg_words[-1]
        print("MONSTER_ID", monster_id)
        monster_descriptions[y][x] = monster_id

    # Updated observation
    obs, done = step.step_stroke(env, '\n')
    return obs, done


def get_cursor_pos(obs) -> (int, int):
    cursor_y, cursor_x = obs['tty_cursor']
    cursor_y -= 1
    return cursor_x, cursor_y


def filtered_monster_properties():
    properties = nethack.permonst.__dict__
    filtered_properties = []
    for entry in properties:
        if type(properties[entry]) != property:
            continue

        filtered_properties.append(entry)

    return filtered_properties


def monster_info():
    filtered_properties = filtered_monster_properties()

    monst_infos = []
    for i in range(381):
        monst_info = nethack.permonst(i)
        monst_dict = dict()
        monst_dict["index"] = i
        for property in filtered_properties:
            monst_dict[property] = monst_info.__getattribute__(property)

        monst_infos.append(monst_dict)

    return monst_infos


def write_monster_info():
    monst_infos = monster_info()

    # open a file for writing
    with open("../../data/monster.json", "w") as outfile:
        # use the json.dump() method to write the list of dictionaries to the file
        json.dump(monst_infos, outfile, indent=2)


if __name__ == "__main__":
    write_monster_info()
