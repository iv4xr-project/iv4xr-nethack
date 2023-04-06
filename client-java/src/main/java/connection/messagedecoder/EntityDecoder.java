package connection.messagedecoder;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import nethack.enums.EntityClass;
import nethack.object.Entity;
import nethack.object.Turn;
import nethack.object.info.EntityInfo;
import util.CustomVec2D;

public class EntityDecoder extends Decoder {
  public static List<Entity> decode(DataInputStream input) throws IOException {
    int nrEntities = input.readByte();
    List<Entity> entities = new ArrayList<>(nrEntities);

    int bytesPerEntity = 13;
    byte[] entityData = input.readNBytes(bytesPerEntity * nrEntities);
    for (int i = 0, offset = 0; i < nrEntities; i++, offset += bytesPerEntity) {
      byte x = entityData[offset];
      byte y = entityData[offset + 1];
      int id =
          parseInt(
              entityData[offset + 2],
              entityData[offset + 3],
              entityData[offset + 4],
              entityData[offset + 5]);
      EntityClass entityClass = toEntityClass(entityData[offset + 6]);
      EntityInfo entityInfo = null;
      Turn createdTurn = new Turn(parseShort(entityData[offset + 9], entityData[offset + 10]), 0);
      int quantity = parseShort(entityData[offset + 11], entityData[offset + 12]);
      Entity entity =
          new Entity(new CustomVec2D(x, y), id, entityClass, entityInfo, createdTurn, quantity);
      entities.add(entity);
    }

    return entities;
  }

  private static EntityClass toEntityClass(byte value) {
    switch (value) {
      case 0:
        return EntityClass.RANDOM;
      case 1:
        return EntityClass.ILLOBJ;
      case 2:
        return EntityClass.WEAPON;
      case 3:
        return EntityClass.ARMOR;
      case 4:
        return EntityClass.RING;
      case 5:
        return EntityClass.AMULET;
      case 6:
        return EntityClass.TOOL;
      case 7:
        return EntityClass.FOOD;
      case 8:
        return EntityClass.POTION;
      case 9:
        return EntityClass.SCROLL;
      case 10:
        return EntityClass.SPELL_BOOK;
      case 11:
        return EntityClass.WAND;
      case 12:
        return EntityClass.COIN;
      case 13:
        return EntityClass.GEM;
      case 14:
        return EntityClass.ROCK;
      case 15:
        return EntityClass.BALL;
      case 16:
        return EntityClass.CHAIN;
      case 17:
        return EntityClass.VENOM;
    }

    throw new RuntimeException(
        String.format("Unknown class, value should be >= 0 and < 18 but was %d", value));
  }
}
