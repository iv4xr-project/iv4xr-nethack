package connection.messagedecoder;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import nethack.object.Monster;
import util.CustomVec2D;
import util.Database;

public class MonsterDecoder extends Decoder {
  public static List<Monster> decode(DataInputStream input) throws IOException {
    List<Monster> monsters = new ArrayList<>();

    int bytesPerMonster = 9;
    int nrMonsters = input.readByte();
    byte[] monsterData = input.readNBytes(bytesPerMonster * nrMonsters);
    for (int i = 0, offset = 0; i < nrMonsters; i++, offset += bytesPerMonster) {
      byte x = monsterData[offset];
      byte y = monsterData[offset + 1];
      int id =
          parseInt(
              monsterData[offset + 2],
              monsterData[offset + 3],
              monsterData[offset + 4],
              monsterData[offset + 5]);
      short permId = parseShort(monsterData[offset + 6], monsterData[offset + 7]);
      boolean isPeaceful = parseBool(monsterData[offset + 8]);
      Monster monster =
          new Monster(
              new CustomVec2D(x, y), id, permId, isPeaceful, Database.getMonsterInfo(permId));
      monsters.add(monster);
    }

    return monsters;
  }
}
