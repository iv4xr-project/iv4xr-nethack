import json
import os
import matplotlib.pyplot as plt
import numpy as np
import inflect


def get_plural(word: str):
    """Gets the plural of a given word, used for labels in the plot"""
    p = inflect.engine()
    return p.plural(word)


def get_total_from_data(data: json, metric: str) -> (np.array, np.array):
    assert data['files'], 'there must be files in the json'

    # Gets the total number of branches or lines per file
    entries = [(file['filename'], file[f'{metric}_total']) for file in data['files']]

    # Sort the entries on the maximally achievable number for the metric and split them up into two lists
    sorted_entries = sorted(entries, key=lambda x: (x[1], x[0]), reverse=True)
    sorted_files = np.array([sorted_entry[0] for sorted_entry in sorted_entries])
    sorted_metric_total = np.array([sorted_entry[1] for sorted_entry in sorted_entries])
    return sorted_files, sorted_metric_total


def get_entries_from_data(data: json, metric: str) -> dict[str, int]:
    """Retrieves the coverage achieved for each file in the json given the metric name"""
    assert data['files'], 'there must be files listed in the json'

    # Added to dictionary since we do not want to rely on ordering here
    entries_dict = dict()
    for file in data['files']:
        entries_dict[file['filename']] = file[f'{metric}_covered']

    return entries_dict


def create_coverage_matrix(coverage_results_paths: [str], metric: str) -> (np.array, np.array, np.ndarray):
    """
    Using the list of files and name of the metric, create a matrix containing the coverage statistics
    Each row in the matrix is a measurement, and each column represents a file
    """
    assert coverage_results_paths, "There must be at least one file in the directory"

    # Get the data about the total number of lines or branches in each file
    with open(coverage_results_paths[0], 'r') as file:
        data = json.load(file)
        file_names, metric_totals = get_total_from_data(data, metric)

    # Array in which each row is a measurement and each column represents a file
    mat = np.zeros((len(coverage_results_paths), len(file_names)), dtype=int)

    # Gather the statistics about the number of lines covered each run
    for i in range(len(coverage_results_paths)):
        file_path = coverage_results_paths[i]

        # Read the number of lines covered in the file
        with open(file_path, 'r') as file:
            data = json.load(file)
            entries = get_entries_from_data(data, metric)

        # Add the number of lines covered to the matrix
        for j in range(len(file_names)):
            file_name = file_names[j]
            mat[i][j] = entries[file_name]

    return file_names, metric_totals, mat


def create_coverage_plot(coverage_results_paths: [str], method: str, metric: str, show_file_names=False):
    # Coverage data in numpy array
    file_names, metric_total, mat = create_coverage_matrix(coverage_results_paths, metric)

    # Set up the bar chart
    if show_file_names:
        fig, ax = plt.subplots(figsize=(19, 6))
    else:
        fig, ax = plt.subplots(figsize=(7, 4))

    index = np.arange(len(file_names))

    if mat.shape[0] == 1:
        # Plot bar for single result
        ax.bar(index, mat[0, :], label=f'Nr. {get_plural(metric)} covered')
    else:
        # Average the coverage results over the number of runs and add error bars
        metric_mean = np.mean(mat, axis=0)
        metric_std = np.std(mat, axis=0)
        ax.bar(index, metric_mean, yerr=metric_std, label=f'Avg. {get_plural(metric)} covered')

    # Plot the maximum that can be reached
    ax.plot(index, metric_total, label=f'Total nr. {get_plural(metric)}', color='black')

    # Add the labels and legends
    ax.set_ylabel(f'Nr. {get_plural(metric)}')
    ax.set_xlabel('File')
    ax.set_title(f'Per-file {metric} coverage using {method} (over {mat.shape[0]} report(s))')

    # Ticks along the x-axis, can be empty
    ax.set_xticks(index)
    if show_file_names:
        short_file_names = [file_name.replace('src/', '') for file_name in file_names]
        ax.set_xticklabels(short_file_names, horizontalalignment='right', rotation=45)
    else:
        ax.set_xticklabels([])

    ax.legend()
    fig.tight_layout()
    fig.align_labels()

    # Save the plot in the plots directory
    os.makedirs("plots", exist_ok=True)
    fig.savefig(f"plots/{metric}_coverage_{method}.png")


def get_coverage_file_names(directory_path: str) -> [str]:
    """
    Get the file names of the coverage reports in a supplied directory
    """
    assert os.path.exists(directory_path)
    file_paths = []

    # Iterate through each file in the directory
    for file_name in os.listdir(directory_path):
        # Check if the file is a regular file (i.e., not a directory)
        if not os.path.isfile(os.path.join(directory_path, file_name)):
            continue

        # Add absolute path to list
        file_paths.append(os.path.abspath(os.path.join(directory_path, file_name)))

    return file_paths


def main():
    iv4xr_coverage_names = get_coverage_file_names('../server-python/coverage')
    create_coverage_plot(iv4xr_coverage_names, 'iv4XR', 'line', True)

    iv4xr_coverage_names = get_coverage_file_names('../server-python/coverage')
    create_coverage_plot(iv4xr_coverage_names, 'iv4XR', 'branch', True)

    # bothack_coverage_names = get_coverage_file_names('../BotHack/coverage')
    # create_coverage_plot(bothack_coverage_names, 'BotHack', 'line', True)


if __name__ == '__main__':
    main()
