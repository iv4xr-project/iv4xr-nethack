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
    if isinstance(obs, np.ndarray):
        if obs.dtype == 'uint8':
            write_obs_byte_list(sock, obs)
            return

    jsonable = util.to_jsonable(env.observation_space, obs)
    sparse_observation = {
        'blstats': jsonable['blstats'],
        'glyphs': jsonable['glyphs'],
    }
    write_field_str(sock, json.dumps(sparse_observation, separators=(',', ':')))


def write_step(sock, done, info, actions):
    actions_str = [str(action) for action in actions]

    step_json = {
        'done': done,
        'info': info,
        'actions': actions_str,
    }
    json_msg = json.dumps(step_json, separators=(',', ':'))
    write_field_str(sock, json_msg)


def write_obs_byte_list(sock, arr):
    """
    Write a byte list observation from a numpy array.
    """
    sock.write(struct.pack('<B', 1))
    dims = list(arr.shape)
    header = struct.pack('<I', len(dims))
    for dim in dims:
        header += struct.pack('<I', dim)
    payload = arr.tobytes()
    sock.write(struct.pack('<I', len(header)+len(payload)))
    sock.write(header)
    sock.write(payload)


def write_reward(sock, rew):
    """
    Write a reward value.
    """
    sock.write(rew)


def write_bool(sock, flag):
    """
    Write a boolean.
    """
    num = 0
    if flag:
        num = 1
    sock.write(struct.pack('<B', num))


def write_action(sock, env, action):
    """
    Write an action object.
    """
    jsonable = util.to_jsonable(env.action_space, action)
    sock.write(struct.pack('<B', 0))
    write_field_str(sock, json.dumps(jsonable))


def write_space(sock, space):
    """
    Encode and write a gym.Space.
    """
    write_field_str(sock, json.dumps(util.space_json(space)))
