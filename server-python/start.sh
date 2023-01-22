#!/bin/bash
# e: exit if fails, x: trace
set -e

# shellcheck disable=SC2154
# echo an error message before exiting
trap 'echo "\"${last_command}\" command filed with exit code $?."' EXIT

# Run script from script directory
BASEDIR=$(dirname "$0")
cd "$BASEDIR"

find . -type f -name "*.gcda" -delete
# shellcheck disable=SC1091
source nethack-server-env/bin/activate
python3 src/main.py

# Remove trap
trap - EXIT
