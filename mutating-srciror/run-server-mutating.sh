#!/bin/bash
# e: exit if fails, x: trace
set -e

# shellcheck disable=SC2154
# echo an error message before exiting
trap 'echo "\"${last_command}\" command filed with exit code $?."' EXIT

# Run script from script directory
BASEDIR=$(cd -- "$(dirname "$0")" >/dev/null 2>&1 ; pwd -P)
cd "$BASEDIR"

# Create mutants
# bash "$BASEDIR"/nethack/hallucination-potion.sh
bash "$BASEDIR"/nethack/camera.sh

SERVER_DIR=$(dirname "$BASEDIR")/server-python
NETHACK_DIR=$(realpath "$SERVER_DIR/lib/nle")
find "$NETHACK_DIR" -name "*.mut.c" | while IFS= read -r file
do
  # Extract word before the first dot
  name="${file%%.*}"
  # Extract extension after the final dot
  extension="${file##*.}"
  # Combine extracted name and extension
  new_file_name="${name}.${extension}"

  echo "Install: $file"
  cp "$file" "$new_file_name"
  bash "$SERVER_DIR"/install.sh
  echo "server: start.sh --exit_on_done"
  bash "$SERVER_DIR"/start.sh --exit_on_done
done

# Remove trap
trap - EXIT
