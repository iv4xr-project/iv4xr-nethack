# iv4xr-nethack

The aim of this project is to use the iv4xr agent to test [NetHack](https://nethack.org) and quantify testability using f.e. achieved coverage.
A socket connection is required since [NLE](https://github.com/facebookresearch/nle) (which contains NetHack) and [iv4xr](https://github.com/iv4xr-project/aplib) are programmed in different languages, Python and Java respectively. A socket connection is established between the Python (server) and Java (client) using messages encoded in [UTF-8](https://www.utf8-chartable.de).

More details on server and client are mentioned in their README.md files.

* [Server README](./server-python/README.md)
* [Client README](./client-java/README.md)

This is for a Thesis project at Utrecht University.
