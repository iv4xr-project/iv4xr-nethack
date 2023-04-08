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
import traceback

# Add path to run from commandline
project_dir = os.path.dirname(os.path.dirname(os.path.dirname(__file__)))
sys.path.append(project_dir)

import src.socket_code.protocol.write as write
import src.socket_code.protocol.read as read
import src.socket_code.protocol.util as util
from src.nethack_util import step
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


CURRENT_ENV = "NetHack-v0"
CURRENT_CHARACTER = "mon-hum-neu-mal"


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
                    env = handle_reset(sock)
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
                    handle_steps(sock, env)
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

    except Exception:
        traceback.print_exc()
        if env:
            env.render()
    finally:
        if env:
            env.close()


def handle_reset(sock):
    """
    Reset the environment and send the result.
    """
    global CURRENT_ENV, CURRENT_CHARACTER

    # Create the environment object
    CURRENT_ENV = read.read_string(sock)
    CURRENT_CHARACTER = read.read_string(sock)
    env = create_env(spawn_monsters=False)

    # Set the seed
    core = read.read_string(sock)
    disp = read.read_string(sock)
    reseed = read.read_bool(sock)
    env.seed(int(core), int(disp), reseed)

    # Write the observation
    write.write_obs(sock, env, env.reset())
    write.write_step(sock, False, None)
    return env


def create_env(save_ttyrec: bool = False, spawn_monsters: bool = True):
    # Settings can be found in: server-python\lib\nle\nle\env\base.py line: 168
    max_episode_steps = 10000000
    # character = "mon-hum-neu-mal"
    # character = "ran-hum-neu-mal"
    # character = "val-hum-neu-fem"
    print(spawn_monsters)
    if save_ttyrec:
        return gym.make(CURRENT_ENV, character=CURRENT_CHARACTER, max_episode_steps=max_episode_steps, save_ttyrec_every=1000000, savedir="nle-recordings", spawn_monsters=spawn_monsters)
    else:
        return gym.make(CURRENT_ENV, character=CURRENT_CHARACTER, max_episode_steps=max_episode_steps, spawn_monsters=spawn_monsters)


def handle_get_seed(sock, env):
    assert CURRENT_ENV == "NetHack-v0"
    seed = env.get_seeds()
    write.write_seed(sock, seed)


def handle_steps(sock, env):
    steps_info = read.read_step_commands(sock)
    obs, done = None, None
    for i in range(int(len(steps_info) / 2)):
        is_nle_command = steps_info[i * 2]
        command = steps_info[i * 2 + 1]
        if is_nle_command:
            obs, done = step.step_action(env, command)
        else:
            obs, done = step.step_stroke(env, chr(command))

    write.write_obs(sock, env, obs)
    write.write_step(sock, done, None)


def handle_save_coverage(sock):
    script_path = os.path.join(project_dir, 'coverage.sh')
    generate_html = read.read_bool(sock)
    if generate_html:
        logging.info(f'calling script: {script_path} html')
        subprocess.call([script_path, "html"])
    else:
        logging.info(f'calling script: {script_path}')
        subprocess.call([script_path])

    write.write_null_byte(sock)


def handle_reset_coverage(sock):
    script_path = os.path.join(project_dir, 'reset-coverage.sh')
    logging.info(f'calling script: {script_path}')
    subprocess.call([script_path])
    write.write_null_byte(sock)


if __name__ == '__main__':
    main()
