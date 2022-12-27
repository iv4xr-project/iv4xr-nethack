# iv4xr-nethack
The aim of this project is to use the iv4xr agent to test [NetHack](https://nethack.org).
Since the original code is in C, and the wrapper is in python a socket connection is used between iv4xr (Java) and nle (Python). Since the application is ran on the python side this is the server. Encoding is utf-8 and messages are in json.

How to run the repository will be stated in the designated README.md files.

This is for a Thesis project of Utrecht University.

## Python side
The NetHack game has been wrapped in python by the [nle package](https://github.com/facebookresearch/nle). It creates a socket server which handles the requests from the iv4xr.

## Java side
Uses the aplib repository to create an environment. Creates a socket connection to communicate and creates a world representation in Java.

