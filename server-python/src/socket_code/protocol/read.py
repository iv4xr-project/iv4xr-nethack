"""
Low-level API for protocol-specific encoding/decoding.
"""
from src.socket_code.protocol.util import ProtoException
import struct

RESET_BYTE = 1
SET_SEED_BYTE = 2
GET_SEED_BYTE = 3
RENDER_BYTE = 4
CLOSE_BYTE = 5
STEP_BYTE = 6
STEP_STROKE_BYTE = 7
SAVE_COVERAGE_BYTE = 8
RESET_COVERAGE_BYTE = 9


def read_byte(sock):
    """
    Read a byte from the socket.
    """
    data = sock.read(1)
    if len(data) != 1:
        raise ProtoException('EOF')
    return struct.unpack('>B', data)[0]


def read_short(sock):
    data = sock.read(2)
    if len(data) != 2:
        raise ProtoException('EOF')
    return struct.unpack('>H', data)[0]


def read_int(sock):
    data = sock.read(4)
    if len(data) != 4:
        raise ProtoException('EOF')
    return struct.unpack('>i', data)[0]


def read_bool(sock):
    """
    Read a bool from the socket.
    """
    data = sock.read(1)
    if len(data) != 1:
        raise ProtoException('EOF')
    return struct.unpack('>?', data)[0]


def read_string(sock):
    length = read_short(sock)
    data = sock.read(length*2)
    if len(data) != length * 2:
        raise ProtoException('EOF')
    data_shorts = struct.unpack(f'>{length}H', data)
    string = ""
    for c in data_shorts:
        string += chr(c)
    return string
