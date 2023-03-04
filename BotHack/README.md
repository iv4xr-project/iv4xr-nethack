# BotHack + NetHack 3.4.3 (nao version)

The BotHack running on the specific NetHack version is used used as a baseline to compare the thesis implementation against for coverage.
We use [BotHack](https://github.com/krajj7/BotHack/) and their mainbot implementation as a play through candidate.
BotHack can be ran on an online server or on a local instance of NetHack. To collect coverage we need to run it local.
Since BotHack only runs on a specific version of NetHack this [version](http://alt.org/nethack/nh343-nao.git) is included under directory [nh343-nao](./nh343-nao).
For any information about that NetHack version, please refer to their [site](https://alt.org/nethack/naonh.php)

Conduct the [BotHack README.md](./BotHack_README.md) to get more information about the bot framework.
The repository has been adapted slightly to analyse coverage and to perform the installation from one shell script:

* Bundle both BotHack and nethack-nao 3.4.3 together
* Add coverage flags to compiler
* Create shell scripts for coverage
* Create install and start script

## Prerequisits

**Please make sure you've already ran the installation for the python-server.**
That installation script will install any additional requirements to compile NetHack from source.

Also, I couldn't get the installation to work in WSL, so this test has only been verified on a designated Ubuntu 23 machine.

## Installation

To make the installation as easy as possible, a shell script has been created. This should perform the entire setup from scratch.

```commandline
sh install.sh
```

## Run

After the installation, running the bot should only require the following commmand.
In the terminal you should see a automatic play through starting on its own and navigating through the map.

```commandline
sh start.sh
```

## Collect coverage

Collecting coverage is the same as in the [python server](../server-python)

To get the statistics of coverage (JSON-format):

```commandline
sh coverage.sh
```

To get a detailed html report:

```commandline
sh coverage.sh html
```

To reset the coverage (since it accumelates over several runs):

```commandline
sh reset-coverage.sh
```
