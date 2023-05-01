package util.jsonConverters;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PotionConverter extends JSONConverter {
  public String getFileName() {
    return "potions";
  }

  @Override
  protected List<ObjectNode> convertUsingReader(BufferedReader br, ObjectMapper mapper)
      throws IOException {
    List<ObjectNode> objectNodes = new ArrayList<>();

    // First line is the header
    String line = br.readLine();

    // Process each line of the input file
    while ((line = br.readLine()) != null) {
      String[] fields = line.split("\t");
      ObjectNode objectNode = mapper.createObjectNode();
      objectNode.put("Name", fields[0]);
      objectNode.put("Cost", Integer.parseInt(fields[1]));
      objectNode.put("Weight", Integer.parseInt(fields[2]));
      objectNode.put("Relative probability", Double.parseDouble(fields[3].replace("%", "")));
      objectNode.put("Appearance", fields[4]);
      objectNodes.add(objectNode);
    }

    return objectNodes;
  }
}
