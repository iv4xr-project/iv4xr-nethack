package nethack.object;

import nethack.enums.CommandEnum;

public class Command {
  public final CommandEnum commandEnum;
  public final String stroke;

  public Command(CommandEnum commandEnum) {
    this.commandEnum = commandEnum;
    this.stroke = commandEnum.stroke;
  }

  public Command(String stroke) {
    commandEnum = CommandEnum.ADDITIONAL_ASCII;
    assert !stroke.startsWith("-") : "Stroke may not start with -";
    assert stroke.length() == 1 : "Max length of stroke is 1";
    this.stroke = stroke;
  }

  public static Command fromStroke(String stroke) {
    CommandEnum commandEnum = CommandEnum.fromValue(stroke);
    if (commandEnum != null) {
      return new Command(commandEnum);
    } else if (stroke.startsWith("-")) {
      return new Command(stroke.substring(1));
    } else {
      return null;
    }
  }

  public String toString() {
    String result = commandEnum.toString();
    if (commandEnum == CommandEnum.ADDITIONAL_ASCII) {
      result += " " + stroke;
    }
    return result;
  }

  public static void main(String[] args) {
    Command a = new Command("a");
    Command b = new Command("b");
    assert !a.stroke.equals(b.stroke);
    System.out.print(a.stroke);
    System.out.print(b.stroke);
  }
}
