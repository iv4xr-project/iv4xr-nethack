# Server Python

*Note: For general information about the repository please refer to the top level [README.md](../README.md)*

The purpose of this directory is to create a socket server which can send the GameState to the java iv4XR agent.

This repository contains an adapted version of [NLE package](https://github.com/facebookresearch/nle). NLE puts NetHack into a [gym environment](https://github.com/openai/gym). The entire sourcecode of NetHack and it's [LICENSE](lib/nle/LICENSE) are included in NLE.

The entrypoint [main.py](src/main.py) starts up a socket server on `127.0.0.1:5001`. On connection, it creates a NetHack game instance and will send the GameState and observations through the socket connection in [JSON](https://www.json.org/json-en.html) format as it receives commands from the client to execute. The JSON messages also allows to render the NetHack in the server terminal.

NLE adaptations:

* Change output directory
* Compile NetHack with coverage flags

## Platform

This Python project can only run on Linux/macOS. It has been compiled and ran on Ubuntu-22.04 on desktop and on WSL.

**Important:** All scripts are ran with `bash` since we need to use commands not available in sh.

## Install

```commandline
bash <path-to>/install.sh
```

## Running

Resets the coverage for the run and starts up the server.

```commandline
bash <path-to>/start.sh
```

*Tip: If you have installed it on WSL this [script](./wsl-start.bat) will start server on load. You might have to change the distribution name if another distribution was used.*

## Coverage

Run coverage via [gcovr](https://gcovr.com/en/stable/):

```commandline
bash <path-to>/coverage.sh
```
