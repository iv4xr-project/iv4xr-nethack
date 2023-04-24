#!/bin/bash
# e: exit if fails, x: trace
set -e

# shellcheck disable=SC2154
# echo an error message before exiting
trap 'echo "\"${last_command}\" command filed with exit code $?."' EXIT

# Run script from script directory
BASEDIR=$(cd -- "$(dirname "$0")" >/dev/null 2>&1 ; pwd -P)
cd "$BASEDIR"

NLE_DIR=$(dirname "$BASEDIR")/server-python/lib/nle
find "$NLE_DIR" -name "*.mut.*" -delete
find "$NLE_DIR" -name "*.orig.c" | while IFS= read -r file
do
  echo "restored: ${file%.orig.c}.c"
  mv "$file" "${file%.orig.c}.c"
done

# Remove trap
trap - EXIT
