# Server python

This server is for running the gym environment of NetHack using the nle package as an interface.
The nle package source code has had slight adaptions to enable coverage.

**Important:** All scripts are ran with __bash__ since we need to use commands not available in sh.

## Install

```commandline
bash <path-to>/install.sh
```

## Running

Resets the coverage for the run and starts up the server.

```commandline
bash <path-to>/start.sh
```

## Coverage

Run [coverage](https://gcovr.com/en/stable/):

```commandline
bash <path-to>/coverage.sh
```


## Windows WSL
After installation, this [script](./wsl-start.bat) starts everything up in one go.