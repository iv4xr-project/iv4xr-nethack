#!/bin/bash
# e: exit if fails, x: trace
set -e

# shellcheck disable=SC2154
# echo an error message before exiting
trap 'echo "\"${last_command}\" command filed with exit code $?."' EXIT

# Run script from script directory
BASEDIR=$(cd -- "$(dirname "$0")" >/dev/null 2>&1 ; pwd -P)
cd "$BASEDIR"

# Check if the first argument is either 'coverage' or 'no-coverage'
if [ "$1" != "coverage" ] && [ "$1" != "no-coverage" ]; then
  echo "Invalid argument: '$1'. Must be 'coverage' or 'no-coverage'."
  exit 1
fi

cp bothack.nethackrc /nh343/.nethackrc

if [ "$1" = "coverage" ] ; then
  sh reset-coverage.sh
elif [ "$1" = "no-coverage" ]; then
  echo ALERT: Coverage will not be collected over this individual run
  sleep 10
fi

LD_LIBRARY_PATH=jta26/jni/linux/ lein run config/shell-config.edn

if [ "$1" = "coverage" ] ; then
    sh coverage.sh
fi

# Remove trap
trap - EXIT
