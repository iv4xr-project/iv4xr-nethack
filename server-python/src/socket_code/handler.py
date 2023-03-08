"""
An executable which is run for each connection.

This treats stdin/stdout as a connection to a client.
"""

from argparse import ArgumentParser
import os
import io
import sys
import logging
import time
import subprocess
import gym
import nle
from gym import Env

# Add path to run from commandline
project_dir = os.path.dirname(os.path.dirname(os.path.dirname(__file__)))
sys.path.append(project_dir)

import src.socket_code.protocol.write as write
import src.socket_code.protocol.read as read
import src.socket_code.protocol.util as util
import src.logger as logger


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

    # In case closing connection gave an error
    try:
        handle(io.BufferedRWPair(in_file, out_file))
    except util.ProtoException as exc:
        logging.error('%s gave error: %s' % (options.addr, str(exc)))


CURRENT_ENV = None # "NetHackChallenge-v0"


def handle(sock):
    """
    Handle a connection from a client.
    """
    env = None
    try:
        # Handle commands from the client as they come in and
        # apply them to the given Gym environment.
        last_point = time.time()
        iterations = 0
        acc_rcv = 0
        acc_done = 0
        while True:
            if iterations == 50:
                print(f"RCV in: {acc_rcv / 50} DONE in: {acc_done / 50}")
                # sys.exit()
            iterations += 1
            message_bit = int(read.read_byte(sock))
            current_time = time.time()
            # print("RCV in:", current_time - last_point)
            acc_rcv += current_time - last_point
            last_point = current_time

            # Handle msg type
            match message_bit:
                case read.RESET_BYTE:
                    logging.debug("Reset")
                    env = handle_reset(sock, env, None)
                case read.SET_SEED_BYTE:
                    logging.debug("Set seed")
                    env = handle_set_seed(sock, env)
                case read.GET_SEED_BYTE:
                    logging.debug("Get seed")
                    handle_get_seed(sock, env)
                case read.RENDER_BYTE:
                    logging.debug("Render")
                    env.render()
                case read.CLOSE_BYTE:
                    logging.debug("Close")
                    env.close()
                case read.STEP_BYTE:
                    logging.debug("Step")
                    handle_step(sock, env)
                case read.STEP_STROKE_BYTE:
                    logging.debug("Step stroke")
                    handle_step_stroke(sock, env)
                case read.SAVE_COVERAGE_BYTE:
                    logging.debug("Save coverage")
                    handle_save_coverage(sock)
                case read.RESET_COVERAGE_BYTE:
                    logging.debug("Reset coverage")
                    handle_reset_coverage(sock)
                case unknown:
                    logging.warning(f'Action "{unknown}" not known')

            current_time = time.time()
            # print("DONE in:", current_time - last_point)
            acc_done += current_time - last_point
            last_point = current_time

    except Exception as ex:
        print(ex)
        if env:
            env.render()
    finally:
        if env:
            env.close()


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
    assert CURRENT_ENV == "NetHack-v0"
    seed = env.get_seeds()
    write.write_seed(sock, seed)
    sock.flush()


def handle_step(sock, env):
    """
    Step the environment and send the result.
    """
    action = read.read_byte(sock)
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


def handle_save_coverage(sock):
    script_path = os.path.join(project_dir, 'coverage.sh')
    summary_type = read.read_string(sock)
    logging.info(f'calling script: {script_path} {summary_type}')
    subprocess.call([script_path, summary_type])
    write.write_null_byte(sock)


def handle_reset_coverage(sock):
    script_path = os.path.join(project_dir, 'reset-coverage.sh')
    logging.info(f'calling script: {script_path}')
    subprocess.call([script_path])
    write.write_null_byte(sock)


if __name__ == '__main__':
    main()
