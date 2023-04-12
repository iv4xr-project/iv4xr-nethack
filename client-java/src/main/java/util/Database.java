package util;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import nethack.object.info.EntityInfo;
import nethack.object.info.FoodInfo;
import nethack.object.info.MonsterInfo;
import nethack.object.info.WeaponInfo;
import util.JSONConverters.FoodConverter;
import util.JSONConverters.JSONConverter;
import util.JSONConverters.WeaponConverter;

public class Database {
  private static final ObjectMapper mapper = new ObjectMapper();
  static Map<String, FoodInfo> foodMap = new HashMap<>();
  static Map<String, WeaponInfo> weaponMap = new HashMap<>();
  static Map<Integer, Integer> glyphToObjectMap = new HashMap<>();
  static List<MonsterInfo> monsterList = new ArrayList<>();
  static List<EntityInfo> entityList = new ArrayList<>();

  static {
    try {
      String jsonString = readJsonFromFile(new FoodConverter().getFileName());
      FoodInfo[] foodInfos = mapper.readValue(jsonString, FoodInfo[].class);
      for (FoodInfo foodInfo : foodInfos) {
        foodMap.put(foodInfo.name, foodInfo);
      }

      String weaponJson = readJsonFromFile(new WeaponConverter().getFileName());
      WeaponInfo[] weaponInfos = mapper.readValue(weaponJson, WeaponInfo[].class);
      for (WeaponInfo weaponInfo : weaponInfos) {
        weaponMap.put(weaponInfo.name, weaponInfo);
      }

      String glyphToObjectJson = readJsonFromFile("../../../../../../server-python/data/mapping");
      Integer[] glyphToObjects = mapper.readValue(glyphToObjectJson, Integer[].class);
      for (int i = 0; i < glyphToObjects.length; i++) {
        glyphToObjectMap.put(i, glyphToObjects[i]);
      }

      String monsterJson = readJsonFromFile("../../../../../../server-python/data/monster");
      MonsterInfo[] monsterData = mapper.readValue(monsterJson, MonsterInfo[].class);
      monsterList.addAll(Arrays.asList(monsterData));

      mapper.configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false);
      String entityJson = readJsonFromFile("../../../../../../server-python/data/entity");
      EntityInfo[] entityData = mapper.readValue(entityJson, EntityInfo[].class);
      entityList.addAll(Arrays.asList(entityData));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static MonsterInfo getMonsterInfo(int index) {
    assert index >= 0 && index <= monsterList.size() : "Index must be within the bounds";
    return monsterList.get(index);
  }

  public static EntityInfo getEntityInfo(int index) {
    assert index >= 0;
    if (index >= entityList.size()) {
      return null;
    }

    return entityList.get(index);
  }

  public static EntityInfo getEntityInfoFromGlyph(int glyph) {
    int objectIndex = glyphToObjectMap.get(glyph);
    assert objectIndex != 5976 : "Object index invalid";
    return getEntityInfo(objectIndex);
  }

  public static FoodInfo getFood(String name) {
    return foodMap.get(name);
  }

  public static WeaponInfo getWeapon(String name) {
    return weaponMap.get(name);
  }

  public static String readJsonFromFile(String fileName) {
    String filePath = Path.of(JSONConverter.jsonDir, fileName + ".json").toString();
    StringBuilder jsonData = new StringBuilder();

    // In try so resource gets closed automatically
    try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
      String line;
      while ((line = br.readLine()) != null) {
        jsonData.append(line).append("\n");
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    return jsonData.toString();
  }

  public static void main(String[] args) {}
}
