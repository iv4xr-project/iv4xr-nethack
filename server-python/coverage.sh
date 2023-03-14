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

# Otherwise just create a json file that contains all coverage information
timestamp=$(date +"%Y-%m-%d_%H-%M-%S")
filename="summary_${timestamp}.json"

if [ "$1" = "html" ] ; then
  # Generates html coverage report
  rm -rf "${BASEDIR}"/coverage/htmlreport/*
  mkdir -p "${BASEDIR}"/coverage/htmlreport
  html_options="--html-details -o ${BASEDIR}/coverage/htmlreport/report.html"
  echo "Generating JSON and HTML report... (${filename})"
else
  html_options=""
  echo "Generating JSON report... (${filename})"
fi

gcovr_command="gcovr -r ${BASEDIR}/lib/nle --filter lib/nle/src --exclude lib/nle/src/nle.c --json-summary-pretty --json-summary=${BASEDIR}/coverage/${filename} $html_options"
eval "$gcovr_command"

# Remove trap
trap - EXIT
