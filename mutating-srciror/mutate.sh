#!/bin/bash
# e: exit if fails, x: trace
set -e

# shellcheck disable=SC2154
# echo an error message before exiting
trap 'echo "\"${last_command}\" command filed with exit code $?."' EXIT

# Run script from script directory
BASEDIR=$(cd -- "$(dirname "$0")" >/dev/null 2>&1 ; pwd -P)
cd "$BASEDIR"

# Function to expand a range of numbers
expand_range() {
    local start end range result
    start=$(echo "$1" | cut -d'-' -f1)
    end=$(echo "$1" | cut -d'-' -f2)

    # Loop through the range and append each number to the result
    for ((i=start; i<=end; i++)); do
        result+=("$i")
    done

    # Join the expanded range with commas
    range=$(IFS=,; echo "${result[*]}")
    echo "$range"
}

# Check if input is provided as an argument
if [ $# -lt 1 ]; then
    echo "Usage: $0 <range_and_values>"
    exit 1
fi

# Get the input from the first argument
INPUT=$1

# Replace all occurrences of 'to' with '-'
INPUT=${INPUT//to/-}

# Split the input by commas
IFS=, read -ra VALUES <<< "$INPUT"

# Create an associative array to store unique values
declare -A unique_values

# Loop through the values and expand any ranges
for value in "${VALUES[@]}"; do
    if [[ $value == *-* ]]; then
        # If the value contains a range, expand it
        expanded_range=$(expand_range "$value")
        # Split the expanded range by commas
        IFS=, read -ra expanded_values <<< "$expanded_range"
        # Add each expanded value to the associative array
        for expanded_value in "${expanded_values[@]}"; do
            unique_values["$expanded_value"]=1
        done
    else
        # Otherwise, add the value to the associative array
        unique_values["$value"]=1
    fi
done

# Sort the unique values
sorted_values=($(echo "${!unique_values[@]}" | tr ' ' '\n' | sort -n))

# Join the sorted values with commas
sorted_values=$(IFS=,; echo "${sorted_values[*]}")
echo "$sorted_values"

rm -rf ~/.srciror
mkdir ~/.srciror
NETHACK_DIR=$(realpath "../server-python/lib/nle")
FILE_PATH=$(realpath "$NETHACK_DIR/src/potion.c")
echo "$FILE_PATH:$sorted_values" > ~/.srciror/coverage

export SRCIROR_LLVM_BIN=$BASEDIR/llvm-build/Release+Asserts/bin
export SRCIROR_LLVM_INCLUDES=$BASEDIR/llvm-build/Release+Asserts/lib/clang/3.8.0/include
export SRCIROR_SRC_MUTATOR=$BASEDIR/SRCMutation/build/mutator

# List of paths
paths=(
    "../src/share"
    "$NETHACK_DIR/include"
    "$NETHACK_DIR/src"
    "$NETHACK_DIR/sys/unix"
    "$NETHACK_DIR/win/tty"
    "$NETHACK_DIR/win/rl"
    "$NETHACK_DIR/build/include"
    "$NETHACK_DIR/build/src"
    "$NETHACK_DIR/build/util"
    "/usr/include"
    # "/usr/lib/gcc/x86_64-linux-gnu/11/"
)

# Initialize include and library options
include_opts=""
library_opts=""

# Loop through the paths and construct the options
for path in "${paths[@]}"; do
    # Check if the path is a directory and exists
    if [[ -d "$path" ]]; then
        include_opts+=" -I${path}"
        library_opts+=" -L${path}"
    fi
done

# Add library paths
library_opts+=" -L/usr/lib/gcc/x86_64-linux-gnu/11/"
library_opts+=" -L/usr/lib/x86_64-linux-gnu/"

# Print the include and library options
echo "Include options: ${include_opts}"
echo "Library options: ${library_opts}"

# # Example clang command with include and library options
# clang_cmd="clang -c myfile.c ${include_opts} ${library_opts} -o myfile.o"
# echo "Example clang command: ${clang_cmd}"

python3 $BASEDIR/PythonWrappers/mutationClang $FILE_PATH -B/usr/lib/gcc/x86_64-linux-gnu/11/ ${include_opts} ${library_opts}
# python3 $BASEDIR/PythonWrappers/mutationClang $FILE_PATH --sysroot=/usr/lib/gcc/x86_64-linux-gnu/12 -L"$NETHACK_DIR"/src -I"$NETHACK_DIR"/src -I"$NETHACK_DIR"/src/share -I"$NETHACK_DIR"/sys/unix -I"$NETHACK_DIR"/win/tty -I"$NETHACK_DIR"/win/rl -I"$NETHACK_DIR"/include -I"$NETHACK_DIR"/build/include -I"$NETHACK_DIR"/build/src -I/usr/include -I/usr/lib/gcc/x86_64-linux-gnu/12/ -L"$NETHACK_DIR"/src/share -L"$NETHACK_DIR"/sys/unix -L"$NETHACK_DIR"/win/tty -L"$NETHACK_DIR"/win/rl -L"$NETHACK_DIR"/include -L"$NETHACK_DIR"/build/include -L"$NETHACK_DIR"/build/src -L/usr/include -L/usr/lib/gcc/x86_64-linux-gnu/12/ -o test -v

# Remove trap
trap - EXIT
