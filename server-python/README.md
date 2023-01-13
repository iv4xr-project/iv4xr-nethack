# Server python
This server is for running the gym environment of NetHack using the nle package as an interface.
The nle package source code has had slight adaptions to enable coverage.

## Install
Has been installed on WSL as well, first the instructions will be given that are WSL specific

### WSL additional steps
```commandline
sudo apt install python3-pip python3-venv bison flex cmake libbz2-dev
```

Create venv using
```commandline
python3 -m venv nethack-server-env
source nethack-server-env/bin/activate
```

Optionally fix line endings
```commandline
sudo apt install dos2unix
find . -type f -exec dos2unix {} \;
```

### Install
These steps will have to be done every time

Make sure you have the following installed using:
```commandline
sudo apt install gcov lcov
```

Install the local nle package in the current venv using:
```commandline
pip install -e ./lib/nle
```

## Running
```commandline
python3 src/main.py
```

## Coverage
Run [coverage](https://gcovr.com/en/stable/):
```commandline
mkdir htmlcov -p
gcovr -r . --html-details -o htmlcov/example.html
```
