#!/bin/bash
# Run script from script directory
BASEDIR=$(cd -- "$(dirname "$0")" >/dev/null 2>&1 || exit ; pwd -P)
cd "$BASEDIR" || exit

MUTATION_DIR=$(dirname "$BASEDIR")
bash "$MUTATION_DIR"/mutate.sh apply.c 91-175
