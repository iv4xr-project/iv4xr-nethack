#!/bin/bash
# e: exit if fails, x: trace
set -e

# shellcheck disable=SC2154
# echo an error message before exiting
trap 'echo "\"${last_command}\" command filed with exit code $?."' EXIT

# Run script from script directory
BASEDIR=$(cd -- "$(dirname "$0")" >/dev/null 2>&1 ; pwd -P)
cd "$BASEDIR"
mkdir -p "${BASEDIR}"/coverage

if [ "$1" = "html" ] ; then
  # Generates html coverage report
  echo "Creates html report"
  rm -rf "${BASEDIR}"/coverage/htmlreport/*
  mkdir -p "${BASEDIR}"/coverage/htmlreport
  gcovr -r "${BASEDIR}"/lib/nle --filter lib/nle/src --exclude lib/nle/src/nle.c --html-details -o "${BASEDIR}"/coverage/htmlreport/report.html
else
  # Otherwise just create a json file that contains all coverage information
  timestamp=$(date +"%Y-%m-%d_%H-%M-%S")
  filename="summary_${timestamp}.json"
  echo "Generating json coverage report... (${filename})"
  gcovr -r "${BASEDIR}"/lib/nle --filter lib/nle/src --exclude lib/nle/src/nle.c --json-summary-pretty --json-summary -o "${BASEDIR}"/coverage/"${filename}"
fi

# Remove trap
trap - EXIT
