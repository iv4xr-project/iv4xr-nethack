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
    zipped_map = zip(jsonable['chars'][0], jsonable['colors'][0])
    entities = np.array([[ai, bi] for ai, bi in zipped_map])
    entities = np.swapaxes(entities, 1, 2)

    sparse_observation = {
        'blstats': jsonable['blstats'][0],
        'entities': entities.tolist(),
        'message': jsonable['message'][0],
    }

    json_dump = json.dumps(sparse_observation, separators=(',', ':'))
    print("WRITE Observation")
    write_field_str(sock, json_dump)


def write_step(sock, done, info,):
    step_json = {
        'done': done,
        'info': info,
    }
    json_dump = json.dumps(step_json, separators=(',', ':'))
    print("WRITE Step")
    write_field_str(sock, json_dump)


def write_space(sock, space):
    """
    Encode and write a gym.Space.
    """
    write_field_str(sock, json.dumps(util.space_json(space)))
