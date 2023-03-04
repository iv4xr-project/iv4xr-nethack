#!/bin/bash
# e: exit if fails, x: trace
set -e

# shellcheck disable=SC2154
# echo an error message before exiting
trap 'echo "\"${last_command}\" command filed with exit code $?."' EXIT

# Run script from script directory
BASEDIR=$(cd -- "$(dirname "$0")" >/dev/null 2>&1 ; pwd -P)
cd "$BASEDIR"

cp bothack.nethackrc /nh343/.nethackrc
LD_LIBRARY_PATH=jta26/jni/linux/ lein run config/shell-config.edn

# Remove trap
trap - EXIT