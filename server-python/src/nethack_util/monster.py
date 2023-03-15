import re
import numpy as np
from nle import nethack

from src.nethack_util import message

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

MONSTER_GLYPHS = glyph_to_is_monster()

def id_monsters(env, obs):
    msg = message.read_obs_msg(obs)
    if msg.__contains__('(n)'):
        return obs, obs['screen_descriptions']

    is_monster = MONSTER_GLYPHS[obs['glyphs']]
    monster_indexes = indexes[is_monster]
    pattern = r"^.*(?:called|named)\s(\S+)"

    monster_descriptions = np.zeros(21 * 79, dtype=int)
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
                monster_descriptions[index] = value
            continue

        # Cannot name a human since already has a name
        character = obs['chars'][y][x]
        if character == ord('@'):
            continue

        print("Unidentified monster detected", x, y, "index:", index)
        obs, done = unique_id_monster(env, obs, x, y)
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
        do_step(env, key.capitalize())
        delta -= 8
    while delta != 0:
        do_step(env, key)
        delta -= 1


def unique_id_monster(env, obs, x, y):
    global counter
    cursor_x, cursor_y = get_cursor_pos(obs)

    # Activate command '#name'
    for char in '#n':
        do_step(env, char)
    do_step(env, '\n')

    # Name a monster
    do_step(env, 'm')

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
    raw_obs, _ = do_step(env, ',')
    msg = message.read_raw_obs_msg(raw_obs)

    # Creature has no name yet
    if not msg.__contains__('called'):
        # Give name character by character
        for char in str(counter):
            do_step(env, char)

        # Increment counter
        counter += 1

    # Updated observation
    raw_obs, done = do_step(env, '\n')
    return env.get_observation(raw_obs), done


def do_step(env, char):
    raw_obs, done = env.nethack.step(ord(char))
    msg = message.read_raw_obs_msg(raw_obs)
    print("Char:", char, 'msg:', msg)
    # env.render()
    return raw_obs, done


def get_cursor_pos(obs) -> (int, int):
    cursor_y, cursor_x = obs['tty_cursor']
    cursor_y -= 1
    return cursor_x, cursor_y
