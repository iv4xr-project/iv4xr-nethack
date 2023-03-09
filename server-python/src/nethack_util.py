import re
import numpy as np
from nle import nethack

# Defined in lib/nle/include/display.h
# Can throw index out of range
# nethack.permonst(nethack.glyph_to_mon(np.array([1281])))

# nethack.glyph_is_monster(1500) returns true/false

counter = 0
indexes = np.arange(0, 21 * 79).reshape(21, 79)

def glyph_to_is_monster():
    glyphs = np.arange(0, nethack.MAX_GLYPH + 1)
    func = np.vectorize(lambda g: nethack.glyph_is_monster(g))
    return func(glyphs)

monster_glyphs = glyph_to_is_monster()

def id_monsters(env, obs):
    is_monster = monster_glyphs[obs['glyphs']]
    monster_indexes = indexes[is_monster]
    pattern = r"^.*(?:called|named)\s(\S+)"

    monster_descriptions = np.zeros(21 * 79, dtype=int)
    descriptions = obs['screen_descriptions']
    for index in monster_indexes:
        x = index % 79
        y = int(index / 79)

        description: str = to_description(descriptions[y, x])
        match = re.search(pattern, description)

        if match:
            word = match.group(1)
            if word.isnumeric():
                value = int(word)
                monster_descriptions[index] = value
        else:
            obs, done = unique_id_monster(env, obs, x, y)

    return obs, monster_descriptions

def to_description(arr):
    func = np.vectorize(lambda t: chr(t))
    char_arr = func(arr)
    return ''.join(char_arr)


def move_cursor(env, key, delta):
    while int(delta / 8) != 0:
        do_step(env, key.capitalize())
        delta -= 8
    while delta != 0:
        do_step(env, key)
        delta -= 1


def unique_id_monster(env, obs, x, y):
    global counter

    cursor_y, cursor_x = obs['tty_cursor']
    cursor_y -= 1
    # env.render()
    # Name
    for char in '#n':
        do_step(env, char)
    do_step(env, '\n')

    # Name a monster
    do_step(env, 'm')

    dx = cursor_x - x
    x_key = 'l' if dx < 0 else 'h'
    dx = abs(dx)
    move_cursor(env, x_key, dx)

    dy = cursor_y - y
    y_key = 'j' if dy < 0 else 'k'
    dy = abs(dy)
    move_cursor(env, y_key, dy)

    do_step(env, ',')

    for char in str(counter):
        do_step(env, char)

    counter += 1

    raw_obs, done = do_step(env, '\n')
    return env._get_observation(raw_obs), done


def do_step(env, char):
    # print("Char:", char)
    obs, done = env.nethack.step(ord(char))
    # env.render()
    return obs, done
