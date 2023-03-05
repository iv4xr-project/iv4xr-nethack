import json
import os
import matplotlib.pyplot as plt
import numpy as np
import inflect


def get_plural(word: str):
    p = inflect.engine()
    return p.plural(word)


def get_entries_from_data(data: json, metric: str) -> [(str, int)]:
    entries = [(file['filename'].replace('src/', ''), file[f'{metric}_total'], file[f'{metric}_covered']) for file in data['files']]
    sorted_entries = sorted(entries, key=lambda x: (x[1], x[0]), reverse=True)
    return sorted_entries


def create_coverage_plot(file_path: str, method: str, metric: str, show_file_names=False):
    # Get the data
    with open(file_path, 'r') as file:
        data = json.load(file)

    # Get the data per file
    xy = get_entries_from_data(data, metric)
    file_labels, line_total, line_amount = zip(*xy)
    print(method, f'({len(file_labels)}):', sorted(file_labels))

    # Set up the bar chart
    if show_file_names:
        fig, ax = plt.subplots(figsize=(19, 6))
    else:
        fig, ax = plt.subplots(figsize=(7, 4))

    index = np.arange(len(file_labels))
    ax.bar(index, line_amount, label=f'Nr. {get_plural(metric)} covered')
    ax.plot(index, line_total, label=f'Total nr. {get_plural(metric)}', color='black')

    # Add the labels and legends
    ax.set_ylabel(f'Nr. {get_plural(metric)}')
    ax.set_xlabel('Files')
    ax.set_title(f'Per-file {metric} coverage using {method}')

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
    fig.savefig(f"plots/{metric}_coverage_{method}.png")


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
    create_coverage_plot(iv4xr_coverage_names[0], 'iv4XR', 'line', True)

    iv4xr_coverage_names = get_coverage_file_names('../server-python/coverage')
    create_coverage_plot(iv4xr_coverage_names[0], 'iv4XR', 'branch', True)

    # bothack_coverage_names = get_coverage_file_names('../BotHack/coverage')
    # create_line_coverage_plot(bothack_coverage_names[0], 'BotHack', 'line', True)


if __name__ == '__main__':
    main()
