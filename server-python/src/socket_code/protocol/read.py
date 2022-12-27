"""
Low-level API for protocol-specific encoding/decoding.
"""

import struct
import json
import src.socket_code.protocol.util as util


def read_json(sock):
    """
    Read variable length json data.
    """
    res = sock.readline()
    return json.loads(res.decode())


def read_byte(sock):
    """
    Read a byte from the socket.
    """
    data = sock.read(1)
    if len(data) != 1:
        raise util.ProtoException('EOF')
    return struct.unpack('1B', data)[0]


def read_packet_type(sock):
    """
    Read packet type from the socket and turn it into a
    human-readable string.
    """
    type_id = read_byte(sock)
    mapping = {0: 'reset', 1: 'step', 2: 'get_space', 3: 'sample_action',
               4: 'monitor', 5: 'render', 6: 'upload', 7: 'universe_configure',
               8: 'universe_wrap'}
    if not type_id in mapping.keys():
        raise util.ProtoException('unknown packet type: ' + str(type_id))
    return mapping[type_id]


def read_field(sock):
    """
    Read a variable length data field.
    """
    len_data = sock.read(4)
    if len(len_data) != 4:
        raise util.ProtoException('EOF reading length field')
    length = struct.unpack('<I', len_data)[0]
    res = sock.read(length)
    if len(res) != length:
        raise util.ProtoException('EOF reading field value')
    return res


def read_field_str(sock):
    """
    Read a variable length string field.
    """
    return read_field(sock).decode('utf-8')


def read_bool(sock):
    """
    Read a boolean.
    """
    flag = read_byte(sock)
    if flag == 0:
        return False
    elif flag == 1:
        return True
    raise util.ProtoException('invalid boolean: ' + str(flag))


def read_action(sock, env):
    """
    Read an action object.
    """
    type_id = read_byte(sock)
    if type_id == 0:
        obj = json.loads(read_field_str(sock))
        return util.from_jsonable(env.action_space, obj)
    raise util.ProtoException('unknown action type: ' + str(type_id))


def read_space_id(sock):
    """
    Read a space ID and convert it to a string.
    """
    space_id = read_byte(sock)
    if space_id == 0:
        return 'action'
    elif space_id == 1:
        return 'observation'
    raise util.ProtoException('unknown space ID: ' + str(space_id))
