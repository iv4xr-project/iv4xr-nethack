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

    start = time.time()
    sock.write(OBS_BYTE)
    # write_field(sock, OBS_BYTE)
    # sock.flush()
    sock.write(struct.pack('>27i', *obs['blstats']))
    # sock.flush()
    sock.write(struct.pack('>256B', *obs['message']))
    # sock.flush()
    write_map(sock, obs['chars'], obs['colors'], obs['glyphs'])
    # sock.flush()
    write_inv(sock, obs['inv_letters'], obs['inv_oclasses'], obs['inv_strs'])
    # sock.flush()
    stop = time.time()
    logging.info("DONE WRITE Observation")

    # print("OBSTIME", stop - start)

    # FLUSHES: READY: 0.03470071792602539 Done = 0.026265687942504883


def write_step(sock, done, info):
    # 'info': info,
    logging.info("WRITE Step")
    write_field(sock, STEP_BYTE)
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
        sock.write(struct.pack(">HB80B", inv_letters[i], inv_oclasses[i], *inv_strs[i]))

def write_map(sock, map_chars, map_colors, map_glyphs):
    """
    Encode the entire map in bytes
    """
    height = len(map_chars)
    width = len(map_chars[0])

    for y in range(height):
        for x in range(width):
            sock.write(struct.pack(">BBH", map_chars[y][x], map_colors[y][x], map_glyphs[y][x]))
        sock.flush()
