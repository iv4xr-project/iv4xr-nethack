package util.jsonConverters;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AmuletConverter extends JSONConverter {
  public String getFileName() {
    return "amulets";
  }

  protected List<ObjectNode> convertUsingReader(BufferedReader br, ObjectMapper mapper)
      throws IOException {
    List<ObjectNode> objectNodes = new ArrayList<>();

    // First line is the header
    String line = br.readLine();

    // Process each line of the input file
    while ((line = br.readLine()) != null) {
      String[] fields = line.split("\t");
      ObjectNode objectNode = mapper.createObjectNode();
      objectNode.put("Amulet", fields[0]);
      objectNode.put("cost", 150);
      objectNode.put("weight", 20);
      if (fields.length >= 3) {
        objectNode.put("When eaten", fields[2]);
      }
      if (fields.length >= 4 && !fields[3].isEmpty()) {
        objectNode.put("notes", fields[3]);
      }
      if (fields.length >= 5) {
        objectNode.put("produces the line...", fields[4]);
      }
      if (fields.length >= 6) {
        objectNode.put("Other sources", fields[5]);
      }
      objectNodes.add(objectNode);
    }

    return objectNodes;
  }
}
