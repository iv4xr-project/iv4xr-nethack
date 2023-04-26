#!/bin/bash
# e: exit if fails, x: trace
set -e

# shellcheck disable=SC2154
# echo an error message before exiting
trap 'echo "\"${last_command}\" command filed with exit code $?."' EXIT

# Run script from script directory
BASEDIR=$(cd -- "$(dirname "$0")" >/dev/null 2>&1 ; pwd -P)
cd "$BASEDIR"

# Parse command line arguments
exit_on_done=false

while [[ "$#" -gt 0 ]]; do
  case "$1" in
    -e|--exit_on_done)
      exit_on_done=true
      shift 1
      ;;
    *)
      echo "Unknown option: $1"
      exit 1
      ;;
  esac
done

# shellcheck disable=SC1091
source nethack-server-env/bin/activate
if $exit_on_done; then
  python3 src/main.py --exit_on_done
else
  python3 src/main.py
fi

# Remove trap
trap - EXIT
