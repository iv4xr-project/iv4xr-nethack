"""
Low-level API for protocol-specific encoding/decoding.
"""
import logging
import struct
import sys

import numpy as np
from src.nethack_util import monster
from src.nethack_util import message
import src.socket_code.protocol.util as util


OBS_BYTE = util.to_byte(1)
STEP_BYTE = util.to_byte(2)
SEED_BYTE = util.to_byte(3)


def to_bool(value: bool):
    return struct.pack('b', value)


def to_2byte(values):
    assert type(values) is not list
    if type(values) is not np.ndarray:
        values = np.array([values])

    assert values.ndim == 1

    n = values.shape[0]
    return struct.pack(f'>{n}H', *values)


def to_4byte(values):
    assert type(values) is not list
    if type(values) is not np.ndarray:
        values = np.array([values])

    assert values.ndim == 1

    n = values.shape[0]
    return struct.pack(f'>{n}i', *values)


def string_to_bytes(ints: np.array, trim=False):
    """
    String to bytes
    """
    assert ints.ndim == 1
    if trim:
        ints = np.trim_zeros(ints)

    length_byte = util.to_byte(ints.shape[0])
    return length_byte + util.to_byte(ints)


def write_null_byte(sock):
    sock.write(struct.pack('>B', 0))
    sock.flush()


def write_str(sock, string: str):
    sock.write(util.to_byte(np.array([len(string)])))
    chars = np.array(list(map(lambda c: ord(c), string)))
    sock.write(util.to_byte(chars))


def write_obs(sock, env, obs):
    """
    Encode and send an observation.
    """
    logging.debug("WRITE Observation")
    obs, msg = message.concat_all_messages(env, obs)

    sock.write(OBS_BYTE)
    sock.write(struct.pack('>27i', *obs['blstats']))
    write_str(sock, msg)

    # GERARD
    write_tiles(sock, obs['til_type'], obs['til_flags'], obs['til_visible'])
    write_map(sock, obs['chars'], obs['colors'], obs['glyphs'])
    write_monsters(sock, obs['mon_id'], obs['mon_permid'], obs['mon_peaceful'])
    write_entities(sock, obs['obj_id'], obs['obj_type'], obs['obj_age'], obs['obj_quan'])
    write_inv(sock, obs['inv_letters'], obs['inv_glyphs'], obs['inv_strs'])


def write_step(sock, done, info):
    # 'info': info,
    logging.debug("WRITE Step")
    sock.write(STEP_BYTE)
    sock.write(to_bool(done))
    sock.flush()


def write_seed(sock, seed):
    logging.debug("WRITE Seed")
    sock.write(SEED_BYTE)
    write_str(sock, str(seed[0]))
    sock.flush()


def write_inv(sock, inv_letters, inv_glyphs, inv_strs):
    """
    Inventory is first a byte with number of items, then byte for
    """
    logging.debug("WRITE Inv")
    nr_items = np.trim_zeros(inv_letters).shape[0]
    sock.write(util.to_byte(nr_items))

    for i in range(nr_items):
        sock.write(struct.pack(">BH80B", inv_letters[i], inv_glyphs[i], *inv_strs[i]))

    sock.flush()

def write_tiles(sock, tiles, flags, visible):
    """
    Encode the entire map in bytes
    """
    # Write tiles
    logging.debug("WRITE Tiles")
    nr_tiles = np.sum(np.sign(tiles))
    sock.write(struct.pack(">H", nr_tiles))
    sock.flush()

    for y in range(21):
        for x in range(79):
            if tiles[y][x] == 0:
                continue

            sock.write(struct.pack(">BBBBb", x, y, tiles[y][x], flags[y][x], visible[y][x]))

    sock.flush()

def write_map(sock, chars, colors, glyphs):
    """
    Encode the entire map in bytes
    """
    # Write tiles
    logging.debug("WRITE Map")
    nr_entities = np.count_nonzero(glyphs != 2359)
    sock.write(struct.pack(">H", nr_entities))
    sock.flush()

    for y in range(21):
        for x in range(79):
            if glyphs[y][x] == 2359:
                continue

            sock.write(struct.pack(">BBBBH", x, y, chars[y][x], colors[y][x], glyphs[y][x]))

    sock.flush()

def write_monsters(sock, ids, perm_ids, peaceful):
    logging.debug("WRITE Monsters")
    nr_monsters = np.sum(np.sign(ids))
    sock.write(struct.pack(">B", nr_monsters))
    sock.flush()

    for y in range(21):
        for x in range(79):
            if ids[y][x] == 0:
                continue

            sock.write(struct.pack(">BBiHb", x, y, ids[y][x], perm_ids[y][x], peaceful[y][x]))

    sock.flush()


def write_entities(sock, ids, types, ages, quantities):
    logging.debug("WRITE Entities")
    nr_entities = np.sum(np.sign(ids))
    sock.write(struct.pack(">B", nr_entities))
    sock.flush()

    for y in range(21):
        for x in range(79):
            if ids[y][x] == 0:
                continue

            sock.write(struct.pack(">BBiHHH", x, y, ids[y][x], types[y][x], ages[y][x], quantities[y][x]))

    sock.flush()
