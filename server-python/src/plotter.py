import json
import os
import matplotlib.pyplot as plt
import numpy as np

# Load the JSON data
filename = 'coverage/report_2023-03-02_11-21-14.json'
assert os.path.exists(filename)
with open(filename, 'r') as f:
    data = json.load(f)

# Get the data for the two types of coverage
branch_data = [(f['filename'], f['branch_covered']) for f in data['files']]
line_data = [(f['filename'], f['line_covered']) for f in data['files']]

# Sort the data by percentage in descending order
branch_data = sorted(branch_data, key=lambda x: x[1], reverse=True)
line_data = sorted(line_data, key=lambda x: x[1], reverse=True)

# Get the labels and percentages for the bars
branch_labels, branch_percentages = zip(*branch_data)
line_labels, line_percentages = zip(*line_data)

# Set up the bar chart
fig, (ax1, ax2) = plt.subplots(nrows=2, sharex=True, figsize=(25, 8))
index = np.arange(len(branch_labels))
bar_width = 0.35

# Plot the bars for branch coverage
ax1.bar(index, branch_percentages, bar_width, label='Branch Coverage', color='blue')

# Plot the bars for line coverage
ax2.bar(index, line_percentages, bar_width, label='Line Coverage', color='red')

# Add the labels and legends
ax1.set_ylabel('Branch Coverage')
ax2.set_ylabel('Line Coverage')
ax2.set_xlabel('Files')
ax1.set_title('Coverage by File')
ax1.set_xticks(index)
ax1.set_xticklabels(branch_labels, horizontalalignment='right', rotation=45)
ax2.set_xticks(index)
ax2.set_xticklabels(line_labels, horizontalalignment='right', rotation=45)
ax1.legend()
ax2.legend()

os.makedirs("plots", exist_ok=True)

fig.tight_layout()
fig.align_labels()
fig.savefig("plots/coverage.png")
