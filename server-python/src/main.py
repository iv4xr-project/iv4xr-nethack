import logger
from argparse import ArgumentParser

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


if __name__ == '__main__':
    logger.initialize()
    main()


# env = gym.make("NetHackScore-v0")
# env.reset()
# # env.render()
# obs, reward, done, info = env.step(0)
# obs, reward, done, info = env.step(2)
# obs, reward, done, info = env.step(1)
# obs, reward, done, info = env.step(5)
# obs, reward, done, info = env.step(4)
# obs, reward, done, info = env.step(3)
# obs, reward, done, info = env.step(1)
# obs, reward, done, info = env.step(2)
# obs, reward, done, info = env.step(4)
# obs, reward, done, info = env.step(3)
# obs, reward, done, info = env.step(1)
# obs, reward, done, info = env.step(2)
#
# env.render()
