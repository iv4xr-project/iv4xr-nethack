def concat_all_messages(env, obs):
    current_msg = read_obs_msg(obs)
    if current_msg == "Unknown command '^M'.":
        return obs, ""

    total_msg = current_msg
    done = False
    while current_msg and current_msg != "Unknown command '^M'." and not current_msg.endswith("(n) ") and not current_msg.endswith("?*] ") and not done:
        obs, _, done, _ = env.step(19)
        current_msg = read_obs_msg(obs)
        if current_msg == "Unknown command '^M'.":
            break

        total_msg += " " + current_msg

    return obs, total_msg


def read_obs_msg(obs) -> str:
    return int_arr_to_str(obs['message'])


def read_raw_obs_msg(raw_obs) -> str:
    return int_arr_to_str(raw_obs[5])


def int_arr_to_str(int_arr):
    char_arr = [chr(val) for val in int_arr if val != 0]
    return ''.join(char_arr)
