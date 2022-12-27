# Server python
This server is for running the gym environment of NetHack using the nle package as an interface.
The nle package has had slight adaptions to enable coverage 

## Install
Make sure you have the following installed using:
```commandline
sudo apt install gcov lcov
```

Install the local nle package in the current venv using:
```commandline
pip install -e ./lib/nle
```

## Coverage
Run [coverage](https://gcovr.com/en/stable/):
```commandline
mkdir htmlcov -p
gcovr -r . --html-details -o htmlcov/example.html
```
