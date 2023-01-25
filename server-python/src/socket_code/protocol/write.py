"""
Low-level API for protocol-specific encoding/decoding.
"""
import json
import logging
import time
import struct

import numpy as np
import src.socket_code.protocol.util as util
from src.socket_code.protocol.util import ProtoException


OBS_BYTE = util.to_byte(1)
STEP_BYTE = util.to_byte(2)
SEED_BYTE = util.to_byte(3)


def write_field(sock, field):
    """
    Write a variable length data field.
    """
    logging.debug(f"WRITING: {field}")
    sock.write(field)

def to_bool(bool):
    return struct.pack('b', bool)


def to_2byte(values: [int]):
    if type(values) is not list and type(values) is not np.ndarray:
        values = [values]
    try:
        n = len(values)
        return struct.pack(f'>{n}H', *values)
    except Exception as e:
        logging.warning(f"to_2byte: {values}")
        raise e

def to_4byte(values: [int]):
    if type(values) is not list and type(values) is not np.ndarray:
        values = [values]
    try:
        n = len(values)
        return struct.pack(f'>{n}i', *values)
    except Exception as e:
        logging.warning(f"to_4byte: {values}")
        raise e

def string_to_bytes(ints: np.array, trim=False):
    """
    String to bytes
    """
    if trim:
        ints = np.trim_zeros(ints)
    length_byte = to_2byte(len(ints))
    return length_byte + to_2byte(ints)


def write_str(sock, string):
    length_byte = to_2byte(len(string))
    chars = [ord(c) for c in string]
    write_field(sock, length_byte + to_2byte(chars))


def write_obs(sock, env, obs):
    """
    Encode and send an observation.
    """
    logging.info("WRITE Observation")

    time_1 = time.time()
    write_field(sock, OBS_BYTE)
    sock.flush()
    time_2 = time.time()
    write_field(sock, to_4byte(obs['blstats']))
    time_3 = time.time()
    write_field(sock, string_to_bytes(obs['message'], True))
    time_4 = time.time()
    write_map(sock, obs['chars'], obs['colors'], obs['glyphs'])
    time_5 = time.time()
    write_inv(sock, obs['inv_letters'], obs['inv_oclasses'], obs['inv_strs'])
    time_6 = time.time()
    logging.info("DONE WRITE Observation")

    # print(time_2 - time_1, time_3- time_2, time_4-time_3, time_5-time_4, time_6 - time_5)

    # FLUSHES: READY: 0.03470071792602539 Done = 0.026265687942504883


def write_step(sock, done, info):
    # 'info': info,
    logging.info("WRITE Step")
    write_field(sock, STEP_BYTE)
    sock.flush()
    write_field(sock, to_bool(done))

def write_seed(sock, seed):
    logging.info("WRITE Seed")
    write_field(sock, SEED_BYTE)
    sock.flush()
    write_str(sock, str(seed[0]))
    write_str(sock, str(seed[1]))
    write_field(sock, to_bool(seed[2]))


def write_inv(sock, inv_letters: [int], inv_oclasses: [int], inv_strs: [int]):
    """
    Inventory is first a byte with number of items, then byte for
    """
    nr_items = len(np.trim_zeros(inv_letters))
    sock.write(util.to_byte([nr_items]))

    for i in range(nr_items):
        letter_byte = to_2byte(inv_letters[i])
        class_byte = util.to_byte(inv_oclasses[i])
        inv_str = string_to_bytes(inv_strs[i], True)

        sock.write(letter_byte + class_byte + inv_str)

def write_map(sock, map_chars, map_colors, map_glyphs):
    """
    Encode the entire map in bytes
    """
    height = len(map_chars)
    width = len(map_chars[0])

    for y in range(height):
        for x in range(width):
            sock.write(struct.pack(">HBH", map_chars[y][x], map_colors[y][x], map_glyphs[y][x]))
        sock.flush()

    sock.flush()
