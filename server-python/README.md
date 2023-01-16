# Server python

This server is for running the gym environment of NetHack using the nle package as an interface.
The nle package source code has had slight adaptions to enable coverage.

## Install

```commandline
sh <path-to>/install.sh
```

Since we cannot use source from shell script itself, these will have to be performed manually.

```commandline
cd server-python
source nethack-server-env/bin/activate
pip install -e ./lib/nle
```

## Running

Resets the coverage for the run and starts up the server

```commandline
sh <path-to>/start.sh
```

## Coverage

Run [coverage](https://gcovr.com/en/stable/):

```commandline
sh <path-to>/coverage.sh
```
