import logging

def read_obs_msg(obs) -> str:
    msg = int_arr_to_str(obs['message'])
    if msg.__contains__('more'):
         logging.warning("MORE was in msg " + msg)
    return msg

def read_raw_obs_msg(raw_obs) -> str:
    return int_arr_to_str(raw_obs[5])

def int_arr_to_str(int_arr):
    char_arr = [chr(val) for val in int_arr if val != bytes(1)]
    return ''.join(char_arr)
