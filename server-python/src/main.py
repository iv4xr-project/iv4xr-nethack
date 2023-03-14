import logger
import gym
import sys
import os
from argparse import ArgumentParser

# Add path to run from commandline
sys.path.append(os.path.dirname(os.path.dirname(__file__)))
import src.socket_code.server as server


def main():
    """
    Parse command-line arguments and invoke server.
    """
    parser = ArgumentParser()
    parser.add_argument('-p', '--port', action='store', type=int,
                        dest='port', default=5001)
    parser.add_argument('-u', '--universe', action='store_true',
                        dest='universe')
    parser.add_argument('-s', '--setup', action='store', type=str,
                        dest='setup_code')
    options = parser.parse_args()
    server.serve(**vars(options))


def nethack_gym():
    print("envs:", gym.envs.registry.all())


if __name__ == '__main__':
    logger.initialize_server()
    main()
