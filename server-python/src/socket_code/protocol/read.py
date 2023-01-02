"""
Low-level API for protocol-specific encoding/decoding.
"""
import json


def read_json(sock):
    """
    Read variable length json data.
    """
    res = sock.readline()
    json_msg = json.loads(res.decode())
    print("RECV:", json_msg)
    return json_msg
