package util.JSONConverters;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public abstract class JSONConverter {
  protected static String textDir = "src/main/resources/items/textInfo";
  public static String jsonDir = "src/main/resources/items/jsonInfo";

  protected abstract List<ObjectNode> convertUsingReader(BufferedReader br, ObjectMapper mapper)
      throws IOException;

  public abstract String getFileName();

  protected void convert() {
    String fileName = getFileName();
    assert !fileName.contains(".") : String.format("Must not have extension, unlike %s", fileName);
    Path filePath = Paths.get(textDir, fileName + ".txt");

    ObjectMapper mapper = new ObjectMapper();
    mapper.enable(SerializationFeature.INDENT_OUTPUT);

    // Resource is automatically closed
    try (BufferedReader br = new BufferedReader(new FileReader(filePath.toFile()))) {
      List<ObjectNode> data = convertUsingReader(br, mapper);
      String jsonFileName = fileName.replace(".txt", ".json");
      writeToFile(fileName, data, mapper);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  protected void writeToFile(String fileName, List<ObjectNode> data, ObjectMapper mapper)
      throws IOException {
    assert !fileName.contains(".")
        : String.format("Must not have an extension, unlike %s", fileName);
    Path filePath = Paths.get(jsonDir, fileName + ".json");
    FileWriter fileWriter = new FileWriter(filePath.toFile());

    fileWriter.write(mapper.writeValueAsString(data));
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
