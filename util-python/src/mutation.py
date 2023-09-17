import re

def main():
    import json

    # Load JSON data from the provided file
    file_path = "../../mutating-srciror/mutation_info.json"

    with open(file_path, "r") as json_file:
        data = json.load(json_file)

    # Extract mutants' line numbers and content
    mutants = data["mutants"]
    latex_table = "\\begin{table}\n"
    latex_table += "\\centering\n"
    latex_table += "\\caption{TODO}\n"
    latex_table = "\\begin{tabular}{|c|c|}\n"
    latex_table += "\\hline\n"
    latex_table += "Line Number & Content \\\\\n"
    latex_table += "\\hline\n"

    for mutant in mutants:
        # To match the number in latex
        line_number = int(mutant["line_number"]) - 52
        content = mutant["content"]
        content = re.sub(r"([{}_#&%$])", r"\\\1", content)

        latex_table += f"{line_number} & {content} \\\\\n"

    latex_table += "\\hline\n"
    latex_table += "\\end{tabular}"
    latex_table += "\\end{table}"

    # Print the LaTeX table to the console
    print(latex_table)


if __name__ == '__main__':
    main()
