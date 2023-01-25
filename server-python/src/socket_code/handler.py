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
import src.logger as logger
import logging
from gym import Env
import universe_plugin


def main():
    """
    Executable entry-point.
    """
    logger.initialize_handler()

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
        logging.error('%s gave error: %s' % (info.addr, str(exc)))


def handshake(sock):
    """
    Perform the initial handshake and return the resulting
    Gym environment.
    """
    logging.debug(f"Init env {CURRENT_ENV}")
    try:
        env = gym.make(CURRENT_ENV)
        return handle_reset(sock, env, CURRENT_ENV)
    except gym.error.Error as gym_exc:
        write.write_field(sock, write.string_to_bytes(str(gym_exc)))
        sock.flush()
        raise gym_exc


def loop(sock, uni, env: Env):
    """
    Handle commands from the client as they come in and
    apply them to the given Gym environment.
    """
    while True:
        logging.info("Reading byte")
        message_bit = read.read_byte(sock)

        # RESET_BYTE = util.to_byte(1)
        # SET_SEED_BYTE = util.to_byte(2)
        # GET_SEED_BYTE = util.to_byte(3)
        # RENDER_BYTE = util.to_byte(4)
        # CLOSE_BYTE = util.to_byte(5)
        # STEP_BYTE = util.to_byte(6)
        # STEP_STROKE_BYTE = util.to_byte(7)

        # Handle msg type
        match int(message_bit):
            case read.RESET_BYTE:
                logging.info("Reset")
                env = handle_reset(sock, env, None)
            case read.SET_SEED_BYTE:
                logging.info("Set seed")
                env = handle_set_seed(sock, env)
            case read.GET_SEED_BYTE:
                logging.info("Get seed")
                handle_get_seed(sock, env)
            case read.RENDER_BYTE:
                logging.info("Render")
                handle_render(env)
            case read.CLOSE_BYTE:
                logging.info("Close")
            case read.STEP_BYTE:
                logging.info("Step")
                handle_step(sock, env)
            case read.STEP_STROKE_BYTE:
                logging.info("Step stroke")
                handle_step_stroke(sock, env)
            case unknown:
                logging.warning(f'Action "{unknown}" not known')

    logging.info('exits loop')


def handle_reset(sock, env, desired_env):
    """
    Reset the environment and send the result.
    """
    global CURRENT_ENV

    if not desired_env:
        desired_env = read.read_string(sock)
    if desired_env != CURRENT_ENV:
        env = gym.make(desired_env)
        CURRENT_ENV = desired_env

    write.write_obs(sock, env, env.reset())
    sock.flush()
    return env


def handle_set_seed(sock, env):
    """
    Set the seed of the next run
    """
    global CURRENT_ENV
    if CURRENT_ENV != "NetHack-v0":
        logging.info(f"Env changed to {CURRENT_ENV} for seeding")
        CURRENT_ENV = "NetHack-v0"
        env = gym.make(CURRENT_ENV)

    core = read.read_string(sock)
    disp = read.read_string(sock)
    reseed = read.read_bool(sock)
    env.seed(int(core), int(disp), reseed)
    return env


def handle_get_seed(sock, env):
    seed = None

    try:
        seed = env.get_seeds()
    except RuntimeError:
        logging.warning('Getting seed failed, not a valid env for this action')

    if seed:
        write.write_seed(sock, seed)
    else:
        write.string_to_bytes(sock, write.string_to_bytes(""))

    sock.flush()


def handle_step(sock, env):
    """
    Step the environment and send the result.
    """
    action = read.read_int(sock)
    obs, rew, done, info = env.step(action)
    write.write_obs(sock, env, obs)
    write.write_step(sock, done, info)
    sock.flush()

def handle_step_stroke(sock, env):
    """
    Step the environment and send the result.
    """
    stroke = chr(read.read_short(sock))
    raw_obs, done = env.nethack.step(ord(stroke))
    # Raw observation needs to get zipped
    obs = env._get_observation(raw_obs)
    write.write_obs(sock, env, obs)
    write.write_step(sock, done, None)
    sock.flush()


def handle_render(env):
    """
    Render the environment.
    """
    env.render()


if __name__ == '__main__':
    main()
