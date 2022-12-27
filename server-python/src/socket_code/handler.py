"""
An executable which is run for each connection.

This treats stdin/stdout as a connection to a client.
"""

from argparse import ArgumentParser
import io
import json
import sys
import logging

import src.socket_code.protocol.write as write
import src.socket_code.protocol.read as read
import src.socket_code.protocol.util as util
import gym
import nle
from gym import wrappers, Env
import universe_plugin


def main():
    """
    Executable entry-point.
    """
    parser = ArgumentParser()
    parser.add_argument('--addr', action='store', type=str, dest='addr')
    parser.add_argument('--fd', action='store', type=int, dest='fd')
    parser.add_argument('--universe', action='store_true', dest='universe')
    parser.add_argument('--setup', action='store', type=str, dest='setup_code')
    options = parser.parse_args()

    # pylint: disable=W0122
    exec(options.setup_code)

    in_file = io.open(options.fd, 'rb', buffering=0)
    out_file = io.open(options.fd, 'wb', buffering=0)
    handle(io.BufferedRWPair(in_file, out_file), options)


def handle(sock_file, info):
    """
    Handle a connection from a client.
    """
    try:
        uni = universe_plugin.Universe(info.universe)
        env = handshake(sock_file)
        try:
            loop(sock_file, uni, env)
        finally:
            if env:
                env.close()
    except util.ProtoException as exc:
        log('%s gave error: %s' % (info.addr, str(exc)))


def handshake(sock):
    """
    Perform the initial handshake and return the resulting
    Gym environment.
    """
    env_name = "NetHackScore-v0"

    # Special no-environment mode.
    if env_name == '':
        write.write_field_str(sock, '')
        sock.flush()
        return None

    try:
        env = gym.make(env_name)
        handle_reset(sock, env)
        return env
    except gym.error.Error as gym_exc:
        write.write_field_str(sock, str(gym_exc))
        sock.flush()
        raise gym_exc


def loop(sock, uni, env: Env):
    """
    Handle commands from the client as they come in and
    apply them to the given Gym environment.
    """
    while True:
        json_msg = read.read_json(sock)
        print('Received msg:', json_msg)

        # Handle msg type
        match str(json_msg['cmd']).lower():
            case 'reset':
                handle_reset(sock, env)
            case 'render':
                handle_render(env)
            case 'close':
                return
            case 'step':
                handle_step(sock, env, json_msg['arg'])
            case 'get_space':
                handle_get_space(sock, env)
            case 'sample_action':
                handle_sample_action(sock, env)
            case 'universe_configure':
                env = handle_universe_configure(sock, uni, env)
            case 'universe_wrap':
                env = handle_universe_wrap(sock, uni, env)
            case unknown:
                logging.warning(f'Action "{unknown}" not known')


def handle_reset(sock, env):
    """
    Reset the environment and send the result.
    """
    write.write_obs(sock, env, env.reset())
    sock.flush()


def handle_step(sock, env, args):
    """
    Step the environment and send the result.
    """
    action = int(args['Action'])
    obs, rew, done, info = env.step(action)
    write.write_obs(sock, env, obs)
    write.write_step(sock, done, info, env.actions)
    sock.flush()


def handle_get_space(sock, env):
    """
    Get information about the action or observation space.
    """
    space_id = read.read_space_id(sock)
    if space_id == 'action':
        write.write_space(sock, env.action_space)
    elif space_id == 'observation':
        write.write_space(sock, env.observation_space)
    sock.flush()


def handle_sample_action(sock, env):
    """
    Generate and send a random action.
    """
    action = env.action_space.sample()
    write.write_action(sock, env, action)
    sock.flush()


def handle_render(env):
    """
    Render the environment.
    """
    env.render()


def handle_universe_configure(sock, uni, env):
    """
    Configure a Universe environment.
    """
    config_json = read.read_field_str(sock)
    try:
        env = uni.configure(env, json.loads(config_json))
        write.write_field_str(sock, '')
    except universe_plugin.UniverseException as exc:
        write.write_field_str(sock, str(exc))
    sock.flush()
    return env


def handle_universe_wrap(sock, uni, env):
    """
    Wrap a Universe environment.
    """
    wrapper_name = read.read_field_str(sock)
    config_json = read.read_field_str(sock)
    try:
        env = uni.wrap(env, wrapper_name, json.loads(config_json))
        write.write_field_str(sock, '')
    except universe_plugin.UniverseException as exc:
        write.write_field_str(sock, str(exc))
    sock.flush()
    return env


def log(msg):
    """
    Log logs a message to the console.
    """
    sys.stderr.write(msg + '\n')


if __name__ == '__main__':
    main()
