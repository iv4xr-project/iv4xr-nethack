"""
Low-level API for protocol-specific encoding/decoding.
"""
import sys
import json
from json.decoder import JSONDecodeError
import logging


def read_json(sock):
    """
    Read variable length json data.
    """
    logging.info("Waiting for json msg")
    res = sock.readline()
    if not res:
        logging.info(f"Close message received")
        sys.exit()

    json_msg = json.loads(res.decode())
    logging.debug(f"RECV: {json_msg}")
    return json_msg
