import json
import os
import sys
import matplotlib.pyplot as plt
import numpy as np


def get_entries_from_data(data: json) -> [(str, int)]:
    entries = [(f['filename'].replace('src/', ''), f['line_total'], f['line_covered']) for f in data['files']]
    sorted_entries = sorted(entries, key=lambda x: (x[1], x[0]), reverse=True)
    return sorted_entries


def create_line_coverage_plot(file_path: str, method: str, show_file_names=False):
    # Get the data
    with open(file_path, 'r') as f:
        data = json.load(f)

    # Get the data per file
    xy = get_entries_from_data(data)
    file_labels, line_total, line_amount = zip(*xy)
    print(method, f'({len(file_labels)}):', sorted(file_labels))

    # Set up the bar chart
    if show_file_names:
        fig, ax = plt.subplots(figsize=(19, 6))
    else:
        fig, ax = plt.subplots(figsize=(7, 4))

    index = np.arange(len(file_labels))
    ax.bar(index, line_amount, label='Nr. lines covered')
    ax.plot(index, line_total, label='Total nr. lines', color='black')

    # Add the labels and legends
    ax.set_ylabel('Line Coverage')
    ax.set_xlabel('Files')
    ax.set_title(f'Per-file coverage using {method}')

    if show_file_names:
        ax.set_xticks(index)
        ax.set_xticklabels(file_labels, horizontalalignment='right', rotation=45)
    else:
        ax.set_xticks(index)
        ax.set_xticklabels([])

    ax.legend()
    os.makedirs("plots", exist_ok=True)

    fig.tight_layout()
    fig.align_labels()
    fig.savefig(f"plots/coverage_{method}.png")


def get_coverage_file_names(directory_path: str) -> [str]:
    assert os.path.exists(directory_path)
    file_paths = []

    # Iterate through each file in the directory
    for file_name in os.listdir(directory_path):
        # Check if the file is a regular file (i.e., not a directory)
        if not os.path.isfile(os.path.join(directory_path, file_name)):
            continue

        file_paths.append(os.path.abspath(os.path.join(directory_path, file_name)))

    return file_paths


def main():
    iv4xr_coverage_names = get_coverage_file_names('../server-python/coverage')
    create_line_coverage_plot(iv4xr_coverage_names[0], 'iv4XR', True)

    bothack_coverage_names = get_coverage_file_names('../BotHack/coverage')
    create_line_coverage_plot(bothack_coverage_names[0], 'BotHack', True)


if __name__ == '__main__':
    main()
