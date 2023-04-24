#!/bin/bash
# e: exit if fails, x: trace
set -e

# shellcheck disable=SC2154
# echo an error message before exiting
trap 'echo "\"${last_command}\" command filed with exit code $?."' EXIT

# Run script from script directory
BASEDIR=$(cd -- "$(dirname "$0")" >/dev/null 2>&1 ; pwd -P)
cd "$BASEDIR"

NETHACK_DIR=$(realpath "$(dirname "$BASEDIR")/server-python/lib/nle")
FILE_PATH=$(realpath "$NETHACK_DIR/src/$1")

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
if [ $# -lt 2 ]; then
    echo "Usage: $0 <filename> <range_and_values>"
    exit 1
fi

if [ ! -f "$FILE_PATH" ]; then
    echo "File does not exist: $FILE_PATH"
    exit 1
fi

# Get the input from the first argument
INPUT=$2

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
mapfile -t sorted_values < <(echo "${!unique_values[@]}" | tr ' ' '\n' | sort -n)

# Join the sorted values with commas
sorted_values_string=$(IFS=,; echo "${sorted_values[*]}")

# ############ Settings for SCRIROR ############
sh reset.sh
rm -rf ~/.srciror
mkdir ~/.srciror
echo "$FILE_PATH:$sorted_values_string" > ~/.srciror/coverage

export SRCIROR_LLVM_BIN=$BASEDIR/llvm-build/Release+Asserts/bin
export SRCIROR_LLVM_INCLUDES=$BASEDIR/llvm-build/Release+Asserts/lib/clang/3.8.0/include
export SRCIROR_SRC_MUTATOR=$BASEDIR/SRCMutation/build/mutator

# List of paths to include
paths=(
    "$NETHACK_DIR/include"
    "$NETHACK_DIR/src"
    "$NETHACK_DIR/sys/unix"
    "$NETHACK_DIR/win/tty"
    "$NETHACK_DIR/win/rl"
    "$NETHACK_DIR/build/include"
    "$NETHACK_DIR/build/src"
    "$NETHACK_DIR/build/util"
    "/usr/include"
)

# Initialize include and library options
include_opts=""

# Loop through the paths and construct the options
for path in "${paths[@]}"; do
    # Check if the path is a directory and exists
    if [[ -d "$path" ]]; then
        include_opts+=" -I${path}"
    fi
done

# Command which mutates the source files
python3 "$BASEDIR"/PythonWrappers/mutationClang "$FILE_PATH" "${include_opts}"

# Remove trap
trap - EXIT