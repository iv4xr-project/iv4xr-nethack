#!/bin/bash
# e: exit if fails, x: trace
set -e

# shellcheck disable=SC2154
# echo an error message before exiting
trap 'echo "\"${last_command}\" command filed with exit code $?."' EXIT

# Run script from script directory
BASEDIR=$(dirname "$0")
cd "$BASEDIR"
mkdir coverage -p

if [ "$1" = "html" ] ; then
  # Generates html coverage report
  echo "Creates an html report"
  rm -rf coverage/htmlreport/*
  mkdir coverage/htmlreport -p
  gcovr -r nh343-nao --html-details -o coverage/htmlreport/report.html
else
  # Otherwise just create a json file that contains all coverage information
  timestamp=$(date +"%Y-%m-%d_%H-%M-%S")
  filename="summary_${timestamp}.json"
  echo "Generates json coverage report (${filename})"
  gcovr -r nh343-nao --json-summary-pretty --json-summary -o coverage/"${filename}"
fi

# Remove trap
trap - EXIT
