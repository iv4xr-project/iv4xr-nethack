"""
An executable which is run for each connection.

This treats stdin/stdout as a connection to a client.
"""

from argparse import ArgumentParser
import os
import io
import sys
import logging

# Add path to run from commandline
sys.path.append(os.path.dirname(os.path.dirname(os.path.dirname(__file__))))

import src.socket_code.protocol.write as write
import src.socket_code.protocol.read as read
import src.socket_code.protocol.util as util
import gym
import nle
from gym import Env
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


CURRENT_ENV = "NetHackChallenge-v0"


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
    try:
        env = gym.make(CURRENT_ENV)
        return handle_reset(sock, env, CURRENT_ENV)
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
        arg = json_msg['arg']

        # Handle msg type
        match str(json_msg['cmd']).lower():
            case 'reset':
                env = handle_reset(sock, env, arg)
            case 'set_seed':
                env = handle_set_seed(env, arg)
            case 'get_seed':
                handle_get_seed(sock, env)
            case 'render':
                handle_render(env)
            case 'close':
                return
            case 'step':
                handle_step(sock, env, int(arg))
            case unknown:
                logging.warning(f'Action "{unknown}" not known')


def handle_reset(sock, env, arg):
    """
    Reset the environment and send the result.
    """
    global CURRENT_ENV

    if arg != CURRENT_ENV:
        env = gym.make(arg)
        CURRENT_ENV = arg

    write.write_obs(sock, env, env.reset())
    sock.flush()
    return env


def handle_set_seed(env, seed):
    """
    Set the seed of the next run
    """
    global CURRENT_ENV
    if CURRENT_ENV != "NetHack-v0":
        CURRENT_ENV = "NetHack-v0"
        env = gym.make(CURRENT_ENV)

    env.seed(int(seed['core']), int(seed['disp']), seed['reseed'])
    return env


def handle_get_seed(sock, env):
    seed = None

    try:
        seed = env.get_seeds()
    except RuntimeError:
        print('Getting seed failed, not a valid env for this action')

    if seed:
        write.write_seed(sock, seed)
    else:
        write.write_field_str(sock, "")

    sock.flush()


def handle_step(sock, env, action):
    """
    Step the environment and send the result.
    """
    obs, rew, done, info = env.step(action)
    write.write_obs(sock, env, obs)
    write.write_step(sock, done, info)
    sock.flush()


def handle_render(env):
    """
    Render the environment.
    """
    env.render()


def log(msg):
    """
    Log logs a message to the console.
    """
    sys.stderr.write(msg + '\n')


if __name__ == '__main__':
    main()
