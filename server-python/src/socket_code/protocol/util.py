import numpy as np
import struct


class ProtoException(Exception):
    """
    Exception type used for all protocol-related errors.
    """
    pass


def to_byte(values):
    assert type(values) is not list
    if type(values) is not np.ndarray:
        values = np.array([values])

    assert values.ndim == 1
    n = values.shape[0]
    return struct.pack(f'>{n}B', *values)
