package util;

import connection.SocketClient;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import nethack.NetHack;
import nethack.enums.CommandEnum;
import nethack.object.Command;
import nethack.object.Seed;
import nethack.object.Turn;
import nl.uu.cs.aplib.utils.Pair;

public class Replay {
  public Seed seed;
  public List<Pair<Turn, List<Command>>> actions = new ArrayList<>();

  static final Pattern pattern = Pattern.compile("(\\d+)\\((\\d+)\\):\\[(.*)\\]");

  public Replay(String fileName) {
    try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
      replaySeed(br.readLine());
      replayActions(br);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void replaySeed(String line) {
    String[] splitLine = line.split(":");
    assert Objects.equals(splitLine[0], "SEED") : "First line must be seed";
    String[] items = splitLine[1].split(" ");
    assert items.length == 2;
    seed = new Seed(items[0], items[1], false);
  }

  private void replayActions(BufferedReader reader) throws IOException {
    String line;
    while ((line = reader.readLine()) != null) {
      Matcher m = pattern.matcher(line);
      boolean foundMatch = m.find();
      assert foundMatch : "Line must be a match";
      Turn turn = new Turn(Integer.parseInt(m.group(1)), Integer.parseInt(m.group(2)));

      String[] items = m.group(3).split(", ");
      List<Command> commands = new ArrayList<>();
      for (String commandString : items) {
        if (commandString.contains("ASCII")) {
          commands.add(new Command(commandString.split(" ")[1]));
        } else {
          commands.add(new Command(CommandEnum.valueOf(commandString)));
        }
      }

      actions.add(new Pair<>(turn, commands));
    }
  }

  public static void main(String[] args) {
    String fileName = Config.getReplayFile();
    Replay replay = new Replay(fileName);

    SocketClient client = new SocketClient();
    NetHack nethack = new NetHack(client, replay.seed);
    for (Pair<Turn, List<Command>> action : replay.actions) {
      assert nethack.gameState.stats.turn.equals(action.fst) : "TURN WAS DIFFERENT";
      nethack.step(action.snd.toArray(new Command[] {}));
    }

    // Render and then loop
    nethack.render();
    nethack.loop();
  }
}
