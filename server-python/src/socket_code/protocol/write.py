"""
Low-level API for protocol-specific encoding/decoding.
"""
import json

import numpy as np
import src.socket_code.protocol.util as util


def write_field(sock, field):
    """
    Write a variable length data field.
    """
    sock.write(field)


def write_field_str(sock, field):
    """
    Write a variable length string field.
    """
    text = field.encode('utf-8') + b'\n'
    write_field(sock, text)


def write_obs(sock, env, obs):
    """
    Encode and send an observation.
    """
    jsonable = util.to_jsonable(env.observation_space, obs)
    zipped_map = zip(jsonable['glyphs'][0], jsonable['chars'][0], jsonable['colors'][0])
    entities = np.array([[glyph, char, color] for glyph, char, color in zipped_map])
    entities = np.swapaxes(entities, 1, 2)

    inv_items = zip(jsonable['inv_letters'][0], jsonable['inv_oclasses'][0], jsonable['inv_strs'][0])
    inv_items = [[item[0], item[1], item[2]] for item in inv_items]

    sparse_observation = {
        'blstats': jsonable['blstats'][0],
        'entities': entities.tolist(),
        'message': jsonable['message'][0],
        'inventory': inv_items,
    }

    # print(jsonable.keys())

    json_dump = json.dumps(sparse_observation, separators=(',', ':'))
    print("WRITE Observation")
    write_field_str(sock, json_dump)


def write_step(sock, done, info):
    if info:
        step_json = {
            'done': done,
            'info': info,
        }
    else:
        step_json = {
            'done': done,
        }
    json_dump = json.dumps(step_json, separators=(',', ':'))
    print("WRITE Step")
    write_field_str(sock, json_dump)

def write_space(sock, space):
    """
    Encode and write a gym.Space.
    """
    write_field_str(sock, json.dumps(util.space_json(space)))


def write_seed(sock, seed):
    seed_json = {
        'core': seed[0],
        'disp': seed[1],
        'reseed': seed[2],
    }
    json_dump = json.dumps(seed_json, separators=(',', ':'))
    print("WRITE Seed")
    write_field_str(sock, json_dump)
