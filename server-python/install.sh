#!/bin/bash
# e: exit if fails, x: trace
set -e

# shellcheck disable=SC2154
# echo an error message before exiting
trap 'echo "\"${last_command}\" command filed with exit code $?."' EXIT

# Run script from script directory
BASEDIR=$(cd -- "$(dirname "$0")" >/dev/null 2>&1 ; pwd -P)
cd "$BASEDIR"

# sudo apt update && sudo apt update -y
# sudo apt install -y gcovr lcov python3-pip python3-venv cmake

# # Whether we're running in WSL
# RESULT=$(grep -i Microsoft /proc/version)
# if [ "$RESULT" ]; then
#   sudo apt install -y bison flex libbz2-dev
# fi

# Check if virtual environment exists
if [ ! -d "nethack-server-env" ]; then
  echo "Creating virtual environment..."
  python3 -m venv nethack-server-env
fi

# shellcheck disable=SC1091
source nethack-server-env/bin/activate

# Check if requirements have been installed
if [ ! -f "requirements_installed.flag" ]; then
  echo "Installing requirements..."
  pip install -r requirements.txt
  touch "requirements_installed.flag"
fi

if [ ! -f "package_installed.flag" ]; then
  echo "Installing package..."
  pip install -e ./lib/nle
  touch "package_installed.flag"
else
  pip install --no-deps -e ./lib/nle
fi

# Remove trap
trap - EXIT
