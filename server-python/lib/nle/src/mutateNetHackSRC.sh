#!/bin/bash

# generate all src mutants (assuming all lines are covered)
CURR_DIR=$( cd $( dirname $0 ) && pwd )
SRCIROR_DIR=~/Downloads/srciror/Examples

rm -rf ~/.srciror
mkdir ~/.srciror
echo "$CURR_DIR/were.c:16-20" > ~/.srciror/coverage
# echo "$CURR_DIR/potion.c:25,26,27,28,29" > ~/.srciror/coverage
# :25,26,27,28,29

export SRCIROR_LLVM_BIN=$SRCIROR_DIR/../llvm-build/Release+Asserts/bin
export SRCIROR_LLVM_INCLUDES=$SRCIROR_DIR/../llvm-build/Release+Asserts/lib/clang/3.8.0/include
export SRCIROR_SRC_MUTATOR=$SRCIROR_DIR/../SRCMutation/build/mutator
python3 $SRCIROR_DIR/../PythonWrappers/mutationClang $CURR_DIR/were.c -I../src/share -I../sys/unix -I../win/tty -I../win/rl -I../include -I../build/include -I../build/src -I/usr/include -I/usr/lib/gcc/x86_64-linux-gnu/12/ -L../src/share -L../sys/unix -L../win/tty -L../win/rl -L../include -L../build/include -L../build/src -L/usr/include -L/usr/lib/gcc/x86_64-linux-gnu/12/ -o test

# ../src/share
# ../sys/unix
# ../win/tty
# ../win/rl
# ../include
# ../build/include
# ../build/src
# /usr/include
# /usr/lib/gcc/x86_64-linux-gnu/12/