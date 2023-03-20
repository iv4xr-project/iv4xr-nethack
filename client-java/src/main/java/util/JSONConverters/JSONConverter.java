package util.JSONConverters;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.json.JSONArray;

public abstract class JSONConverter {
  protected String textDir = "src/main/resources/items/textInfo";
  protected String jsonDir = "src/main/resources/items/jsonInfo";

  protected abstract JSONArray convertUsingReader(BufferedReader br) throws IOException;

  abstract void convert();

  protected void convertFile(String fileName) {
    assert fileName.endsWith(".txt") : String.format("Must read text file, not %s", fileName);
    Path filePath = Paths.get(textDir, fileName);

    // Resource is automatically closed
    try (BufferedReader br = new BufferedReader(new FileReader(filePath.toFile()))) {
      JSONArray data = convertUsingReader(br);
      String jsonFileName = fileName.replace(".txt", ".json");
      writeToFile(jsonFileName, data);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  protected void writeToFile(String fileName, JSONArray data) throws IOException {
    assert fileName.endsWith(".json") : String.format("Must write to json file, not %s", fileName);
    Path filePath = Paths.get(jsonDir, fileName);
    FileWriter fileWriter = new FileWriter(filePath.toFile());
    fileWriter.write(data.toString(4));
    fileWriter.write(System.lineSeparator());
    fileWriter.close();
  }

  // Convert all classes
  public static void main(String[] args) {
    new AmuletConverter().convert();
    new ArmorConverter().convert();
    new FoodConverter().convert();
    new GemsConverter().convert();
    new PotionConverter().convert();
    new RingConverter().convert();
    new ScrollConverter().convert();
    new SpellbookConverter().convert();
    new ToolConverter().convert();
    new WandConverter().convert();
    new WeaponConverter().convert();
  }
}
