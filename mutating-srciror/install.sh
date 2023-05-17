#!/bin/bash
# e: exit if fails, x: trace
set -e

# shellcheck disable=SC2154
# echo an error message before exiting
trap 'echo "\"${last_command}\" command filed with exit code $?."' EXIT

# Install JQuery to process json data
sudo apt install -y jq

# Run script from script directory
BASEDIR=$(cd -- "$(dirname "$0")" >/dev/null 2>&1 ; pwd -P)
cd "$BASEDIR"

# Build llvm
sh ./llvm-build.sh

# Build the mutation
cd SRCMutation
make

# Remove trap
trap - EXIT
