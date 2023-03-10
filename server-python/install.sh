#!/bin/bash
# e: exit if fails, x: trace
set -e

# shellcheck disable=SC2154
# echo an error message before exiting
trap 'echo "\"${last_command}\" command filed with exit code $?."' EXIT

# Run script from script directory
BASEDIR=$(cd -- "$(dirname "$0")" >/dev/null 2>&1 ; pwd -P)
cd "$BASEDIR"

sudo apt update && sudo apt update -y
sudo apt install -y gcovr lcov python3-pip python3-venv cmake

# Whether we're running in WSL
RESULT=$(grep -i Microsoft /proc/version)
if [ "$RESULT" ]; then
  sudo apt install -y bison flex libbz2-dev
fi

python3 -m venv nethack-server-env
# shellcheck disable=SC1091
source nethack-server-env/bin/activate
pip install -e ./lib/nle
pip install -r requirements.txt

# Remove trap
trap - EXIT
