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

############ Settings for SCRIROR ############
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

############ PERFORM MUTATION ############
cp "$FILE_PATH" "${FILE_PATH%.c}.orig.c"
python3 "$BASEDIR"/PythonWrappers/mutationClang "$FILE_PATH" "${include_opts}"

############ CREATE FILE WITH MUTATION INFORMATION ############
declare -A line_numbers

# Use the find command to locate the files and iterate over them
while IFS= read -r -d '' file; do
  # Extract the line number
  line_number=$(echo "$file" | grep -oP '(?<=\.)(\d+)(?=\.)')

  # Check if the line number is already in the array
  if [[ -z ${line_numbers[$line_number]} ]]; then
    # Add the line number to the array if it's unique
    line_numbers[$line_number]=1
  fi
done < <(find "$NETHACK_DIR/src" -name "*.mut.c" -print0 | sort -z)

# Sort the keys in ascending order
line_numbers=("$(printf '%s\n' "${!line_numbers[@]}" | sort -n)")

# Iterate over the line numbers
json_original=$(
  while IFS= read -r number; do
    line_content=$(sed -n "${number}p" "$FILE_PATH")
    jq -n --arg number "$number" --arg content "$line_content" '{line_number: $number, content: $content}'
  done <<< "$(echo -e "${!line_numbers[@]}")" | jq -n '.original |= [inputs]'
)

# Use the find command to locate the files and iterate over them
json_mutants=$(
  find "$NETHACK_DIR/src" -name "*.mut.c" -print0 | sort -z | while IFS= read -r -d '' file; do
    relative_path=$(realpath --relative-to="$NETHACK_DIR/src" "$file")
    # Extract the line number
    line_number=$(echo "$file" | grep -oP '(?<=\.)(\d+)(?=\.)')
    # Extract the line content
    content=$(sed -n "${line_number}p" "$file")
    # Print the formatted output
    jq -n --arg number "$line_number" --arg path "$relative_path" --arg content "$content" '{path: $path, line_number: $number, content: $content}'
  done | jq -n '.mutants |= [inputs]'
)

relative_file_path=$(realpath --relative-to="$NETHACK_DIR/src" "$FILE_PATH")
json_lines=$(printf '%s\n' "${sorted_values[@]}" | jq -R '. | tonumber' | jq -s .)

combined_object=$(jq -n \
  --arg file "$relative_file_path" \
  --argjson lines "$json_lines" \
  --argjson original "$json_original" \
  --argjson mutants "$json_mutants" \
  '{file: $file} + {lines: $lines} + $original + $mutants')

echo "$combined_object" > mutation_info.json

# Remove trap
trap - EXIT
