import json
import os
import sys
import matplotlib.pyplot as plt
import numpy as np


def get_entries_from_data(data: json) -> [(str, int)]:
    entries = [(f['filename'], f['line_total'], f['line_covered']) for f in data['files']]
    sorted_entries = sorted(entries, key=lambda x: (x[1], x[0]), reverse=True)
    return sorted_entries


def create_line_coverage_plot(iv4xr_file_path: str, bothack_file_path: str):
    # Get the data
    with open(iv4xr_file_path, 'r') as f:
        iv4xr_data = json.load(f)

    with open(bothack_file_path, 'r') as f:
        bothack_data = json.load(f)

    xy1 = get_entries_from_data(iv4xr_data)
    xy2 = get_entries_from_data(bothack_data)

    # Get the labels and percentages for the bars
    file_labels1, line_total1, line_amount1 = zip(*xy1)
    file_labels2, line_total2, line_amount2 = zip(*xy2)

    print(f'NetHack 3.6.6 ({len(file_labels1)}): ', sorted(file_labels1))
    print(f'NetHack 3.4.3 nao ({len(file_labels2)}): ', sorted(file_labels2))
    sys.exit()

    # Set up the bar chart
    fig, ax = plt.subplots(figsize=(25, 4))
    index = np.arange(len(file_labels1))
    bar_width = 0.35

    # Plot the bars for line coverage
    ax.bar(index, line_amount1, bar_width, label='Line Coverage iv4XR', color='red')
    # ax.bar(index, line_amount2, bar_width, label='Line Coverage BotHack', color='blue')
    ax.plot(index, line_total1, label='Nr lines', color='black')

    # Add the labels and legends
    ax.set_ylabel('Line Coverage')
    ax.set_xlabel('Files')
    ax.set_title('Coverage by File')
    ax.set_xticks(index)
    ax.set_xticklabels(file_labels1, horizontalalignment='right', rotation=45)
    ax.legend()

    os.makedirs("plots", exist_ok=True)

    fig.tight_layout()
    fig.align_labels()
    fig.savefig("plots/coverage.png")



# def create_line_coverage_plot(iv4xr_file_name: str, bothack_file_name: str):
#     # Get the data for the two types of coverage
#     # branch_data = [(f['filename'], f['branch_covered']) for f in data['files']]
#     line_data = [(f['filename'], f['line_covered']) for f in data['files']]
#
#     # Sort the data by percentage in descending order
#     # branch_data = sorted(branch_data, key=lambda x: x[1], reverse=True)
#     line_data = sorted(line_data, key=lambda x: x[1], reverse=True)
#
#     # Get the labels and percentages for the bars
#     # branch_labels, branch_percentages = zip(*branch_data)
#     line_labels, line_percentages = zip(*line_data)
#
#     # Set up the bar chart
#     fig, (ax1, ax2) = plt.subplots(nrows=2, sharex=True, figsize=(25, 8))
#     index = np.arange(len(branch_labels))
#     bar_width = 0.35
#
#     # Plot the bars for branch coverage
#     ax1.bar(index, branch_percentages, bar_width, label='Branch Coverage', color='blue')
#
#     # Plot the bars for line coverage
#     ax2.bar(index, line_percentages, bar_width, label='Line Coverage', color='red')
#
#     # Add the labels and legends
#     ax1.set_ylabel('Branch Coverage')
#     ax2.set_ylabel('Line Coverage')
#     ax2.set_xlabel('Files')
#     ax1.set_title('Coverage by File')
#     ax1.set_xticks(index)
#     ax1.set_xticklabels(branch_labels, horizontalalignment='right', rotation=45)
#     ax2.set_xticks(index)
#     ax2.set_xticklabels(line_labels, horizontalalignment='right', rotation=45)
#     ax1.legend()
#     ax2.legend()
#
#     os.makedirs("plots", exist_ok=True)
#
#     fig.tight_layout()
#     fig.align_labels()
#     fig.savefig("plots/coverage.png")


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
    bothack_coverage_names = get_coverage_file_names('../BotHack/coverage')

    create_line_coverage_plot(iv4xr_coverage_names[0], bothack_coverage_names[0])


if __name__ == '__main__':
    main()
