package util;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import nethack.object.data.Food;
import nethack.object.data.Weapon;
import opennlp.tools.stemmer.PorterStemmer;
import util.JSONConverters.FoodConverter;
import util.JSONConverters.JSONConverter;
import util.JSONConverters.WeaponConverter;

public class Database {
  private static final ObjectMapper mapper = new ObjectMapper();
  private static final PorterStemmer porterStemmer = new PorterStemmer();
  static Map<String, Food> foodMap = new HashMap<>();

  static Map<String, Weapon> weaponMap = new HashMap<>();

  static {
    try {
      String jsonString = readJsonFromFile(new FoodConverter().getFileName());
      Food[] foods = mapper.readValue(jsonString, Food[].class);
      for (Food food : foods) {
        foodMap.put(stemName(food.name), food);
      }

      String weaponJson = readJsonFromFile(new WeaponConverter().getFileName());
      Weapon[] weapons = mapper.readValue(weaponJson, Weapon[].class);
      for (Weapon weapon : weapons) {
        weaponMap.put(stemName(weapon.name), weapon);
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static Food getFood(String name) {
    return foodMap.get(stemName(name));
  }

  public static Weapon getWeapon(String name) {
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
