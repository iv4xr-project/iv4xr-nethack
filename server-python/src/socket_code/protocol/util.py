from gym import spaces
import numpy as np


class ProtoException(Exception):
    """
    Exception type used for all protocol-related errors.
    """
    pass


def from_jsonable(space, obj):
    """
    Decode a space element from JSON.
    """
    if isinstance(space, spaces.Tuple):
        return tuple(
            [from_jsonable(space, obj[i]) for i, space in enumerate(space.spaces)]
        )
    return space.from_jsonable([obj])[0]


def to_jsonable(space, obj):
    """
    Encode a space element as JSON.
    """
    if isinstance(space, spaces.Tuple):
        return [to_jsonable(space, obj[i]) for i, space in enumerate(space.spaces)]
    return space.to_jsonable([obj])  # [0]


def space_json(space):
    """
    Encode a gym.Space as JSON.
    """
    if isinstance(space, spaces.Box):
        # JSON doesn't support infinity.
        bound = 1e30
        return {
            'type': 'Box',
            'shape': space.shape,
            'low': np.clip(space.low, -bound, bound).flatten().tolist(),
            'high': np.clip(space.high, -bound, bound).flatten().tolist()
        }
    elif isinstance(space, spaces.Discrete):
        return {
            'type': 'Discrete',
            'n': space.n
        }
    elif isinstance(space, spaces.MultiBinary):
        return {
            'type': 'MultiBinary',
            'n': space.n
        }
    elif isinstance(space, spaces.MultiDiscrete):
        return {
            'type': 'MultiDiscrete',
            'low': space.low.tolist(),
            'high': space.high.tolist()
        }
    elif isinstance(space, spaces.Tuple):
        return {
            'type': 'Tuple',
            'subspaces': [space_json(sub) for sub in space.spaces]
        }
    return {
        'type': type(space).__name__
    }
