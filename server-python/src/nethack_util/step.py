from src.nethack_util import message


def step_action(env, command):
    assert type(command) == int, f"Instead type is '{type(command)}'"
    action = env.actions[command]
    obs, _, done, _ = env.step(command)
    # print("Action:", action, "msg:", message.read_obs_msg(obs))
    return obs, done

def step_stroke(env, char):
    assert len(char) == 1, f"Length of '{char}' is {len(char)}, but must be 1"
    command = ord(char)
    raw_obs, done = env.nethack.step(command)
    obs = env.get_observation(raw_obs)
    # print("Char:", char, "msg:", message.read_obs_msg(obs))
    return obs, done

def step_strokes(env, string):
    assert type(string) == str
    assert len(string) > 0
    obs, done = None, None
    for char in string:
        obs, done = step_stroke(env, char)

    return obs, done
