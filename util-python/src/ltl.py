def check_level_nr():
    with open("../ltl/levelNr.txt", "r") as file:
        lines = file.readlines()

    for i in range(1, len(lines)):
        prev_line =  lines[i - 1].strip()
        prev_level_txt = prev_line.split(":")[2].strip()
        prev_level = int(prev_level_txt)
        current_line = lines[i].strip()
        current_level_txt = current_line.split(":")[2].strip()
        current_level = int(current_level_txt)

        if current_level < prev_level:
            print(f"LVLNR: Line {i + 1} violates the increasing levelNr property:", current_line)


def check_location():
    with open("../ltl/location.txt", "r") as file:
        lines = file.readlines()

    prev_level = None
    prev_x, prev_y = None, None

    for i in range(0, len(lines)):
        line = lines[i].strip()

        level_nr_txt = line.split(":")[2].strip().split("<")[1]
        level_nr = int(level_nr_txt)
        coordinate_txt = line.split("<")[2].split(">")[0].split(",")
        current_x, current_y = map(int, coordinate_txt)

        if prev_level is not None and level_nr != prev_level:
            prev_level = level_nr
            prev_x, prev_y = current_x, current_y
            continue

        if prev_x and prev_y:
            dx = abs(current_x - prev_x)
            dy = abs(current_y - prev_y)
            if dx > 1 or dy > 1:
                print(f"LOCATION: Line {i + 1} violates the adjacent coordinates property:", line)

        prev_level = level_nr
        prev_x, prev_y = current_x, current_y


def ltl_score():
    with open("../ltl/score.txt", "r") as file:
        lines = file.readlines()

    for i in range(1, len(lines)):
        prev_line = lines[i - 1].strip()
        prev_level_txt = prev_line.split(":")[2].strip()
        prev_level = int(prev_level_txt)
        current_line = lines[i].strip()
        current_level_txt = current_line.split(":")[2].strip()
        current_level = int(current_level_txt)

        if current_level < prev_level:
            print(f"SCORE: Line {i + 1} violates the increasing score property:", current_line)


def main():
    check_level_nr()
    check_location()
    ltl_score()


if __name__ == '__main__':
    main()
