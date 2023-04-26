package nethack.object;

import nethack.enums.CommandEnum;

public class Command {
  public final CommandEnum commandEnum;
  public final String stroke;

  public Command(CommandEnum commandEnum) {
    this.commandEnum = commandEnum;
    this.stroke = commandEnum.stroke;
  }

  public Command(char character) {
    commandEnum = CommandEnum.ADDITIONAL_ASCII;
    assert character != '-' : "Command must not be - it is special character";
    this.stroke = String.valueOf(character);
  }

  public static Command fromStroke(String stroke) {
    CommandEnum commandEnum = CommandEnum.fromValue(stroke);
    if (commandEnum != null) {
      return new Command(commandEnum);
    } else if (stroke.startsWith("-")) {
      return new Command(stroke.charAt(1));
    } else {
      return null;
    }
  }

  public static Command fromLiteralStroke(String stroke) {
    return fromStroke("-" + stroke);
  }

  public String toString() {
    String result = commandEnum.toString();
    if (commandEnum == CommandEnum.ADDITIONAL_ASCII) {
      result += " " + stroke;
    }
    return result;
  }

  public static void main(String[] args) {
    Command a = new Command('a');
    Command b = new Command('b');
    assert !a.stroke.equals(b.stroke);
    System.out.print(a.stroke);
    System.out.print(b.stroke);
  }
}
