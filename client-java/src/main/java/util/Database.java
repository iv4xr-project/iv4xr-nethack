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
import opennlp.tools.stemmer.PorterStemmer;
import util.JSONConverters.FoodConverter;
import util.JSONConverters.JSONConverter;
import util.JSONConverters.WeaponConverter;

public class Database {
  private static final ObjectMapper mapper = new ObjectMapper();
  private static final PorterStemmer porterStemmer = new PorterStemmer();
  static Map<String, FoodInfo> foodMap = new HashMap<>();
  static Map<String, WeaponInfo> weaponMap = new HashMap<>();
  static List<MonsterInfo> monsterList = new ArrayList<>();
  static List<EntityInfo> entityList = new ArrayList<>();

  static {
    try {
      String jsonString = readJsonFromFile(new FoodConverter().getFileName());
      FoodInfo[] foodInfos = mapper.readValue(jsonString, FoodInfo[].class);
      for (FoodInfo foodInfo : foodInfos) {
        foodMap.put(stemName(foodInfo.name), foodInfo);
      }

      String weaponJson = readJsonFromFile(new WeaponConverter().getFileName());
      WeaponInfo[] weaponInfos = mapper.readValue(weaponJson, WeaponInfo[].class);
      for (WeaponInfo weaponInfo : weaponInfos) {
        weaponMap.put(stemName(weaponInfo.name), weaponInfo);
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

  public static FoodInfo getFood(String name) {
    return foodMap.get(stemName(name));
  }

  public static WeaponInfo getWeapon(String name) {
    return weaponMap.get(stemName(name));
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

  public static String stemName(String name) {
    String[] comps = name.split(" ");
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < comps.length; i++) {
      if (i != 0) {
        sb.append(" ");
      }
      sb.append(porterStemmer.stem(comps[i]));
    }

    return sb.toString();
  }

  public static void main(String[] args) {}
}
