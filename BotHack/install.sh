#!/bin/bash
# e: exit if fails, x: trace
set -e

# shellcheck disable=SC2154
# echo an error message before exiting
trap 'echo "\"${last_command}\" command filed with exit code $?."' EXIT

# Run script from script directory
BASEDIR=$(cd -- "$(dirname "$0")" >/dev/null 2>&1 ; pwd -P)
cd "$BASEDIR"

sudo apt update && sudo apt update -y
sudo apt install -y gcovr lcov ant leiningen openjdk-8-jdk libncurses5-dev libncursesw5-dev

printf "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!\nPLEASE SELECT openjdk-8-jdk\nOtherwise perform an ant clean in the jta26 directory\n!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!\n"
sudo update-alternatives --config java

cd jta26
ant

cd jni/linux
make

# Install NetHack 3.4.3 nao version
cd "$BASEDIR"
cd nh343-nao
make
sudo make install

# Clear directory
sudo rm -rf /nh343
sudo mv /opt/nethack/nethack.alt.org/nh343 /nh343
sudo chmod -R 777 /nh343

# Remove trap
trap - EXIT
