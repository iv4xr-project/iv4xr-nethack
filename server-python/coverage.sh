#!/bin/bash
# e: exit if fails, x: trace
set -e

# shellcheck disable=SC2154
# echo an error message before exiting
trap 'echo "\"${last_command}\" command filed with exit code $?."' EXIT

# Run script from script directory
BASEDIR=$(dirname "$0")
cd "$BASEDIR"

# Remove old directory
rm -rf htmlcov/*
mkdir htmlcov -p
gcovr -r . --html-details -o htmlcov/example.html

# Remove trap
trap - EXIT
