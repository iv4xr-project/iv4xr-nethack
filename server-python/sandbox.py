import gym
import nle
from nle import nethack

env = gym.make("NetHack-v0")
env.reset()
env.render()
aaa = env.last_observation
print(aaa)
