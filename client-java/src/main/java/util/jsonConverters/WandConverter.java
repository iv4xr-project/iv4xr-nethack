package util.jsonConverters;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class WandConverter extends JSONConverter {
  public String getFileName() {
    return "wands";
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
      objectNode.put("Weight", 7);
      objectNode.put("Name", fields[0]);
      objectNode.put("Cost", Integer.parseInt(fields[1]));
      if (!fields[2].equals("varies")) {
        String[] chargesString = fields[2].split("–");
        ObjectNode charges = mapper.createObjectNode();
        charges.put("min", Integer.parseInt(chargesString[0]));
        charges.put("max", Integer.parseInt(chargesString[1]));
        objectNode.set("Charges", charges);
      }
      objectNode.put("Relative probability", Double.parseDouble(fields[3].replace("%", "")));
      objectNode.put("Type", fields[4]);
      objectNodes.add(objectNode);
    }

    return objectNodes;
  }
}
